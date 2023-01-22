package com.secjar.secjarapi.services;

import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.exceptions.RoleNotFoundException;
import com.secjar.secjarapi.models.UserRole;
import com.secjar.secjarapi.repositories.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public UserRole getRole(UserRolesEnum role) {
        return roleRepository.findByRole(role).orElseThrow(() -> new RoleNotFoundException(String.format("Role %s does not exist", role)));
    }
}