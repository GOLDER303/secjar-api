package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.SupportRequestDTO;
import com.secjar.secjarapi.dtos.requests.SupportSubmissionCreateRequestDTO;
import com.secjar.secjarapi.dtos.requests.SupportSubmissionPatchRequestDTO;
import com.secjar.secjarapi.enums.SupportSubmissionStatesEnum;
import com.secjar.secjarapi.exceptions.BadEmailException;
import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import com.secjar.secjarapi.models.SupportSubmission;
import com.secjar.secjarapi.models.SupportSubmissionNote;
import com.secjar.secjarapi.repositories.SupportSubmissionRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupportSubmissionService {

    private final SupportSubmissionRepository supportSubmissionRepository;
    private final SupportSubmissionNoteService supportSubmissionNoteService;

    public SupportSubmissionService(SupportSubmissionRepository supportSubmissionRepository, SupportSubmissionNoteService supportSubmissionNoteService) {
        this.supportSubmissionRepository = supportSubmissionRepository;
        this.supportSubmissionNoteService = supportSubmissionNoteService;
    }

    public void saveSupportSubmission(SupportSubmission supportSubmission) {
        supportSubmissionRepository.save(supportSubmission);
    }

    public List<SupportSubmission> getPendingSubmissions() {
        List<Optional<SupportSubmission>> pendingSubmissions = supportSubmissionRepository.findAllByState(SupportSubmissionStatesEnum.PENDING);

        if (pendingSubmissions.isEmpty()) {
            return Collections.emptyList();
        }

        return pendingSubmissions.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }

    public SupportSubmission getSubmissionByUuid(String supportSubmissionUuid) {
        return supportSubmissionRepository.findByUuid(supportSubmissionUuid).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with uuid %s does not exist", supportSubmissionUuid)));
    }

    public void patchSupportSubmission(String supportSubmissionUuid, SupportSubmissionPatchRequestDTO supportSubmissionPatchRequestDTO) {
        SupportSubmission supportSubmission = getSubmissionByUuid(supportSubmissionUuid);

        if (supportSubmissionPatchRequestDTO.submissionStatus() != null) {
            supportSubmission.setState(supportSubmissionPatchRequestDTO.submissionStatus());
        }

        saveSupportSubmission(supportSubmission);
    }

    public String addNote(String supportSubmissionUuid, SupportSubmissionCreateRequestDTO supportSubmissionCreateRequestDTO) {
        SupportSubmission supportSubmission = getSubmissionByUuid(supportSubmissionUuid);

        SupportSubmissionNote supportSubmissionNote = new SupportSubmissionNote(UUID.randomUUID().toString(), supportSubmissionCreateRequestDTO.content(), supportSubmission);

        supportSubmissionNoteService.saveSupportSubmissionNote(supportSubmissionNote);

        return supportSubmissionNote.getUuid();
    }

    public String createNewSubmission(SupportRequestDTO supportRequestDTO) {
        boolean isValidEmail = EmailValidator.getInstance().isValid(supportRequestDTO.email());

        if(!isValidEmail) {
            throw new BadEmailException(String.format("%s is not a valid email", supportRequestDTO.email()));
        }

        SupportSubmission supportSubmission = new SupportSubmission(
                UUID.randomUUID().toString(),
                supportRequestDTO.name(),
                supportRequestDTO.surname(),
                supportRequestDTO.email(),
                supportRequestDTO.message());

        saveSupportSubmission(supportSubmission);

        return supportSubmission.getUuid();
    }
}
