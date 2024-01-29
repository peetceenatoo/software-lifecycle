package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorInConnectingToGitHubException;
import com.polimi.PPP.CodeKataBattle.Exceptions.MissingEnvironmentVariableExcpetion;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
@NoArgsConstructor
public class GitHubAPI implements IGitHubAPI {

    private GitHub gitHub;

    @Value("${CKB_GITHUB_TOKEN}")
    private String oAuthToken;

    public GitHubAPI(Boolean useForTesting){
        String variableValue = System.getenv("CKB_GITHUB_TOKEN");
        this.oAuthToken = variableValue;
        this.init();
    }

    @PostConstruct
    public void init() {

        if (oAuthToken == null || oAuthToken.isEmpty()){
            throw new MissingEnvironmentVariableExcpetion("Missing GitHub OAuth token");
        }

        try{
            gitHub = new GitHubBuilder().withJwtToken(oAuthToken).build();
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in connecting to GitHub, check token");
        }

    }
    @Override
    public String createRepository(String name, String description, boolean isPrivate){
        GHRepository repository;
        try{
            repository = gitHub.createRepository(name)
                    .private_(isPrivate)
                    .description(description)
                    .create();
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in creating repository");
        }
        try{
        repository.addCollaborators(gitHub.getUser("GabP404"));
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in adding collaborator");
        }


        return repository.getHtmlUrl().toString();
    }
    @Override
    public void pushFile(String repositoryName, String filePath, String commitMessage) {
        this.pushFile(repositoryName, filePath, commitMessage, "main");
    }
    @Override
    public void pushFile(String repositoryName, String filePath, String commitMessage, String branchName){
        this.pushFile(repositoryName, filePath, "", commitMessage, branchName);
    }
    @Override
    public void pushFile(String repositoryName, String filePath, String baseFolder, String commitMessage, String branchName){
        GHRepository repo;
        try{
            repo = this.gitHub.getRepository(repositoryName);
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in connecting to the repo to push file");
        }

        Path path = Paths.get(filePath);

        try {
            String content = new String(Files.readAllBytes(path));

            repo.createContent()
                    .content(content)
                    .message(commitMessage)
                    .branch(branchName)
                    .commit();
        } catch (IOException e) {
            throw new ErrorInConnectingToGitHubException("Error in pushing file");
        }
    }
    @Override
    public void pushFolder(String repositoryName, String folderPath, String commitMessage){
        this.pushFolder(repositoryName, folderPath, commitMessage, "main");
    }
    @Override
    public void pushFolder(String repositoryName, String folderPath, String commitMessage, String branchName){
        this.pushFolder(repositoryName, folderPath, "", commitMessage, branchName);
    }
    @Override
    public void pushFolder(String repositoryName, String folderPath, String baseFolder, String commitMessage, String branchName){
        GHRepository repo;
        try{
            repo = this.gitHub.getRepository(repositoryName);
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in connecting to the repo to push file");
        }

        Path pathToFolder = Paths.get(folderPath);
        try (Stream<Path> paths = Files.walk(pathToFolder)) {
            paths.filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            String content = new String(Files.readAllBytes(filePath));
                            String relativePath = pathToFolder.relativize(filePath).toString();

                            repo.createContent()
                                    .content(content)
                                    .path(relativePath)
                                    .message(commitMessage + "\n" + "Add/update file " + relativePath)
                                    .branch(branchName) // optional
                                    .commit();
                        } catch (IOException e) {
                            throw new ErrorInConnectingToGitHubException("Error in pushing folder");
                        }
                    });
        } catch (IOException e) {
            throw new ErrorInConnectingToGitHubException("Error in pushing folder");
        }
    }



    @Override
    public String createRepository(String name, String description, boolean isPrivate, String homepage) {

        GHRepository repository;
        try{
            repository = gitHub.createRepository(name)
                    .private_(isPrivate)
                    .description(description)
                    .homepage(homepage)
                    .create();
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in creating repository");
        }

        return repository.getHtmlUrl().toString();
    }

    @Override
    public void changeRepositoryVisibility(String repositoryName, boolean isPrivate){
        GHRepository repository;
        try{
            repository = gitHub.getRepository(repositoryName);
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in connecting to repository");
        }

        try{
            repository.setVisibility(isPrivate ? GHRepository.Visibility.PRIVATE : GHRepository.Visibility.PUBLIC);
        }catch (IOException e){
            throw new ErrorInConnectingToGitHubException("Error in changing repository visibility");
        }
    }

    @Override
    public void cloneRepository(String repositoryName, String downloadPath) {

        try {
            if (Files.exists(Paths.get(downloadPath))) {
                deleteFolder(Paths.get(downloadPath));
                //throw new ErrorInConnectingToGitHubException("Error in cloning repository, folder already exists");
            }
            Files.createDirectories(Paths.get(downloadPath));
        }catch(IOException ex){
            throw new ErrorInConnectingToGitHubException("Error in cloning repository");
        }

        GHRepository repository;
        try{
            repository = gitHub.getRepository(repositoryName);
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in connecting to repository");
        }



        try{
            downloadContents(repository, "", downloadPath);
        }catch (Exception e){
            throw new ErrorInConnectingToGitHubException("Error in cloning repository");
        }
    }

    @Override
    public void cloneRepository(String repositoryName, String downloadPath, String commitHash){

        try {
            if (Files.exists(Paths.get(downloadPath))) {
                deleteFolder(Paths.get(downloadPath));
                //throw new ErrorInConnectingToGitHubException("Error in cloning repository, folder already exists");
            }
            Files.createDirectories(Paths.get(downloadPath));
        }catch(IOException ex){
            throw new ErrorInConnectingToGitHubException("Error in cloning repository");
        }

        try {
            // Step 2: Access the Repository
            GHRepository repo = this.gitHub.getRepository(repositoryName);

            // Step 3: Get a specific commit
            GHCommit commit = repo.getCommit(commitHash);

            downloadContents(repo, commit, "", downloadPath);

        } catch (IOException e) {
            throw new ErrorInConnectingToGitHubException("Error in cloning repository");
        }
    }

    private void downloadContents(GHRepository repo, GHCommit commit, String path, String downloadDir) throws IOException {
        List<GHContent> contents = repo.getDirectoryContent(path, commit.getSHA1());
        for (GHContent content : contents) {
            Path localPath = Path.of(downloadDir, content.getPath());
            if (content.isDirectory()) {
                Files.createDirectories(localPath);
                downloadContents(repo, commit, content.getPath(), downloadDir);
            } else {
                content.read().transferTo(Files.newOutputStream(localPath));
            }
        }
    }

    private void downloadContents(GHRepository repo, String path, String downloadDir) throws IOException {
        List<GHContent> contents = repo.getDirectoryContent(path);
        for (GHContent content : contents) {
            Path localPath = Path.of(downloadDir, content.getPath());
            if (content.isDirectory()) {
                Files.createDirectories(localPath);
                downloadContents(repo, content.getPath(), downloadDir);
            } else {
                content.read().transferTo(Files.newOutputStream(localPath));
            }
        }
    }

    private void deleteFolder(Path folderPath) throws IOException {
        if (Files.isDirectory(folderPath)) {
            try (var stream = Files.list(folderPath)) {
                stream.forEach(subPath -> {
                    try {
                        deleteFolder(subPath);
                    } catch (IOException e) {
                        throw new ErrorInConnectingToGitHubException("Error in deleting folder");
                    }
                });
            }
        }
        Files.deleteIfExists(folderPath);
    }

}
