package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorInConnectingToGitHubException;
import com.polimi.PPP.CodeKataBattle.Exceptions.MissingEnvironmentVariableExcpetion;
import jakarta.annotation.PostConstruct;
import org.kohsuke.github.GHContentBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Component
public class GitHubAPI implements IGitHubAPI {

    private GitHub gitHub;

    @Value("${CKB_GITHUB_TOKEN}")
    private String oAuthToken;

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
}
