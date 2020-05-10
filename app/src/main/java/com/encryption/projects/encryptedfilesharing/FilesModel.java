package com.encryption.projects.encryptedfilesharing;

public class FilesModel {
    String fileId, fileName, fileExtension, fileDescription, fileSize, fileDateTime, fileMobile, filePath;

    public FilesModel(String fileId, String fileName, String fileExtension, String fileDescription, String fileSize, String fileDateTime, String fileMobile, String filePath) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileDescription = fileDescription;
        this.fileSize = fileSize;
        this.fileDateTime = fileDateTime;
        this.fileMobile = fileMobile;
        this.filePath = filePath;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public void setFileDescription(String fileDescription) {
        this.fileDescription = fileDescription;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileDateTime() {
        return fileDateTime;
    }

    public void setFileDateTime(String fileDateTime) {
        this.fileDateTime = fileDateTime;
    }

    public String getFileMobile() {
        return fileMobile;
    }

    public void setFileMobile(String fileMobile) {
        this.fileMobile = fileMobile;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
