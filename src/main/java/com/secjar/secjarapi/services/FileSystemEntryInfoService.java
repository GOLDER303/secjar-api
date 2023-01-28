package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileSystemEntryInfo;
import com.secjar.secjarapi.repositories.FileSystemEntryInfoRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileSystemEntryInfoService {

    private final FileSystemEntryInfoRepository fileSystemEntryInfoRepository;

    public FileSystemEntryInfoService(FileSystemEntryInfoRepository fileSystemEntryInfoRepository) {
        this.fileSystemEntryInfoRepository = fileSystemEntryInfoRepository;
    }

    public void saveFileSystemEntryInfo(FileSystemEntryInfo fileSystemEntryInfo) {
        fileSystemEntryInfoRepository.save(fileSystemEntryInfo);
    }

    public void deleteFileSystemEntryInfoByUuid(String fileSystemEntryInfoUuid){
        fileSystemEntryInfoRepository.deleteByUuid(fileSystemEntryInfoUuid);
    }

    public FileSystemEntryInfo findFileSystemEntryInfoByUuid(String fileSystemEntryInfoUuid) {
        //TODO: create custom exception
        return fileSystemEntryInfoRepository.findByUuid(fileSystemEntryInfoUuid).orElseThrow(() -> new RuntimeException(String.format("FileSystemEntryInfo with uuid: %s does not exist", fileSystemEntryInfoUuid)));
    }

    public List<FileSystemEntryInfo> findAllWithDeleteDateLessThan(Timestamp timestamp) {
        List<Optional<FileSystemEntryInfo>> filesToDelete = fileSystemEntryInfoRepository.findAllByDeleteDateLessThan(timestamp);

        if(filesToDelete.isEmpty()) {
            return Collections.emptyList();
        }

        return filesToDelete.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }

    public void removeDeleteDate(String fileSystemEntryInfoUuid) {
        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoRepository.findByUuid(fileSystemEntryInfoUuid).orElseThrow(() -> new RuntimeException(String.format("FileSystemEntryInfo with uuid: %s does not exist", fileSystemEntryInfoUuid)));

        fileSystemEntryInfo.setDeleteDate(null);

        fileSystemEntryInfoRepository.save(fileSystemEntryInfo);
    }

    public void moveFileToDirectory(FileSystemEntryInfo file, FileSystemEntryInfo directory) {
        file.setParent(directory);
        directory.getChildren().add(file);

        fileSystemEntryInfoRepository.save(file);
        fileSystemEntryInfoRepository.save(directory);
    }
}
