package com.polimi.PPP.CodeKataBattle.Utilities;

public interface IGitHubAPI {

    String createRepository(String name, String description, boolean isPrivate, String homepage);

    String createRepository(String name, String description, boolean isPrivate);

    void pushFile(String usernameOwner, String repositoryName, String filePath, String commitMessage);
    void pushFile(String usernameOwner, String repositoryName, String filePath, String commitMessage, String branchName);
    void pushFile(String usernameOwner, String repositoryName, String filePath, String baseFolder, String commitMessage, String branchName);
    void pushFolder(String usernameOwner, String repositoryName, String folderPath, String commitMessage);
    void pushFolder(String usernameOwner, String repositoryName, String folderPath, String commitMessage, String branchName);
    void pushFolder(String usernameOwner, String repositoryName, String folderPath, String baseFolder, String commitMessage, String branchName);
    void changeRepositoryVisibility(String usernameOwner, String repositoryName, boolean isPrivate);
}
