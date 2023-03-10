package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.constrains.UserConstrains;

public record UserPatchRequestDTO(Long fileDeletionDelay, Long allowedDiskSpace) implements UserConstrains {

    public UserPatchRequestDTO {
        if (fileDeletionDelay != null) {
            if (fileDeletionDelay < MIN_FILE_DELETION_DELAY) {
                throw new IllegalArgumentException(String.format("File deletion delay cannot be smaller than %d ms", MIN_FILE_DELETION_DELAY));
            }
            if (fileDeletionDelay > MAX_FILE_DELETION_DELAY) {
                throw new IllegalArgumentException(String.format("File deletion delay cannot be larger than %d ms", MAX_FILE_DELETION_DELAY));
            }
        }

        if (allowedDiskSpace != null) {
            if (allowedDiskSpace < 0) {
                throw new IllegalArgumentException("Allowed disk space cannot be negative");
            }
        }
    }
}
