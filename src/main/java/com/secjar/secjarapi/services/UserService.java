package com.secjar.secjarapi.services;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.RegistrationRequestDTO;
import com.secjar.secjarapi.dtos.requests.UserPatchRequestDTO;
import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.repositories.UserRepository;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final HsmService hsmService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService, HsmService hsmService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.hsmService = hsmService;
    }

    public User getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException(String.format("User with uuid: %s does not exist", uuid)));
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(String.format("User with id: %s does not exist", id)));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("User with username: %s does not exist", username)));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format("User with email: %s does not exist", email)));
    }

    public boolean checkIfUserWithEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean checkIfUserWithUsernameExist(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    public User createUserFromRegistrationRequest(RegistrationRequestDTO registrationRequestDTO) {
        return new User(
                UUID.randomUUID().toString(),
                registrationRequestDTO.username(),
                passwordEncoder.encode(registrationRequestDTO.password()),
                registrationRequestDTO.email(),
                List.of(roleService.getRole(UserRolesEnum.ROLE_USER))
        );
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public int enableUser(String email) {
        return userRepository.enableAppUser(email);
    }

    public void addCryptoKeyToUser(long id) {
        User user = getUserById(id);

        CryptoServerCXI.Key key = hsmService.generateKey(String.format("%s's key", user.getUuid()));
        byte[] keyIndex = hsmService.insertKeyToStore(key);

        user.setCryptographicKeyIndex(keyIndex);

        saveUser(user);
    }

    public void changeUserPasswordByUuid(String userUuid, String newPassword) {
        User user = getUserByUuid(userUuid);

        //TODO: check for password strength
        user.setPassword(passwordEncoder.encode(newPassword));

        saveUser(user);
    }

    public void changeUserPasswordByEmail(String userEmail, String newPassword) {
        User user = getUserByEmail(userEmail);

        //TODO: check for password strength
        user.setPassword(passwordEncoder.encode(newPassword));

        saveUser(user);
    }

    public void pathUser(String userUuid, UserPatchRequestDTO userPatchRequestDTO) {
        User user = getUserByUuid(userUuid);

        if (userPatchRequestDTO.fileDeletionDelay() != null) {
            user.setFileDeletionDelay(userPatchRequestDTO.fileDeletionDelay());
        }

        if (userPatchRequestDTO.desiredSessionTime() != null) {
            user.setDesiredSessionTime(userPatchRequestDTO.desiredSessionTime());
        }

        saveUser(user);
    }

    public boolean verifyUserPassword(String userUuid, String password) {
        User user = getUserByUuid(userUuid);

        return passwordEncoder.matches(password, user.getPassword());
    }

    public String generateQRUrl(User user) {
        QrGenerator generator = new ZxingPngQrGenerator();

        QrData data = new QrData.Builder()
                .label(user.getEmail())
                .secret(user.getMFASecret())
                .issuer("SecJar")
                .algorithm(HashingAlgorithm.SHA512)
                .digits(6)
                .period(30)
                .build();


        byte[] imageData;

        try {
             imageData = generator.generate(data);
        } catch (QrGenerationException e) {
            throw new RuntimeException(e);
        }

        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    public void updateUserMFA(String userUuid, boolean use2FA) {
        User user = getUserByUuid(userUuid);

        user.setUsingMFA(use2FA);

        saveUser(user);
    }
}
