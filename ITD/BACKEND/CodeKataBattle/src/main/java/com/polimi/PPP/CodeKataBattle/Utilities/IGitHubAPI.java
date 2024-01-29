package com.polimi.PPP.CodeKataBattle.Utilities;

public interface IGitHubAPI {

    String createRepository(String name, String description, boolean isPrivate, String homepage);

    String createRepository(String name, String description, boolean isPrivate);

    void cloneRepository(String repositoryName, String downloadPath);
    void cloneRepository(String repositoryName, String downloadPath, String commitHash);
    void pushFile(String repositoryName, String filePath, String commitMessage);
    void pushFile(String repositoryName, String filePath, String commitMessage, String branchName);
    void pushFile(String repositoryName, String filePath, String baseFolder, String commitMessage, String branchName);
    void pushFolder(String repositoryName, String folderPath, String commitMessage);
    void pushFolder(String repositoryName, String folderPath, String commitMessage, String branchName);
    void pushFolder(String repositoryName, String folderPath, String baseFolder, String commitMessage, String branchName);
    void changeRepositoryVisibility(String repositoryName, boolean isPrivate);
}
