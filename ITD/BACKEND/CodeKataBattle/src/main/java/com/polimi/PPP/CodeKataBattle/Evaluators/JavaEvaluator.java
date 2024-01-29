package com.polimi.PPP.CodeKataBattle.Evaluators;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.SubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorDuringEvaluationException;
import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorInConnectingToGitHubException;
import com.polimi.PPP.CodeKataBattle.Utilities.CustomLogger;
import com.polimi.PPP.CodeKataBattle.Utilities.IGitHubAPI;
import org.apache.maven.shared.invoker.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;


public class JavaEvaluator implements IEvaluator{

    private final IGitHubAPI gitHubAPI;
    private final String workingDirectory = "/Users/japo/Desktop/CodeKataBattle/";

    public JavaEvaluator(IGitHubAPI gitHubAPI) {
        this.gitHubAPI = gitHubAPI;

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

    private String downloadCodeForEvaluation(SubmissionDTO submission) throws ErrorDuringEvaluationException{
        BattleDTO battle = submission.getBattle();
        String userSubmissionRepositoryUrl = submission.getRepositoryUrl();
        String userSubmissionCommitHash = submission.getCommitHash();
        String battleTestRepositoryUrl = battle.getTestRepositoryLink();

        //Generate UUID for temp folder
        String tempFolderName = UUID.randomUUID().toString();

        //Create temp folder for evaluation
        String tempFolder = workingDirectory + tempFolderName;
        Path tempFolderPath = Paths.get(tempFolder);
        if (Files.exists(tempFolderPath)) {
            try {
                deleteFolder(tempFolderPath);
            } catch (IOException e) {
                throw new ErrorDuringEvaluationException("Error in deleting folder");
            }
        }
        try {
            Files.createDirectories(tempFolderPath);
        } catch (IOException e) {
            throw new ErrorDuringEvaluationException("Error in creating folder");
        }

        //Clone user submission repository
        try{
            this.gitHubAPI.cloneRepository(userSubmissionRepositoryUrl, tempFolder + "/userCode", userSubmissionCommitHash);
            this.gitHubAPI.cloneRepository(battleTestRepositoryUrl, tempFolder + "/testCode");
        }catch (ErrorInConnectingToGitHubException ex){
            throw new ErrorDuringEvaluationException("Error in cloning repository");
        }

        //Copy src main java folder from user submission repository to test repository
        Path userSubmissionPath = Paths.get(tempFolder + "/userCode");
        Path testPath = Paths.get(tempFolder + "/testCode");
        Path srcMainJavaPath = Paths.get(tempFolder + "/userCode/src/main/java");
        Path testSrcMainJavaPath = Paths.get(tempFolder + "/testCode/src/main/java");

        try {
            copyFolder(srcMainJavaPath, testSrcMainJavaPath);
        } catch (Exception e) {
            throw new ErrorDuringEvaluationException("Error in copying files");
        }

        return tempFolder;
    }

    public static void copyFolder(Path source, Path destination) throws IOException {

        if (!Files.exists(destination)) {
            Files.createDirectories(destination);
        }
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectory(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    public Float scoreOfFunctionalTests(SubmissionDTO submission) throws ErrorDuringEvaluationException {


        String tempFolder = downloadCodeForEvaluation(submission);
        Path tempFolderPath = Paths.get(tempFolder);
        String testPath = tempFolder + "/testCode";
        String testPomPath = testPath + "/pom.xml";

        //Compile test repository
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(testPomPath));
        request.setGoals(Collections.singletonList("clean install")); // Use "clean install" to build and test

        Invoker invoker = new DefaultInvoker();
        File mavenExecutable;
        //Choose correct maven executable
        if (System.getProperty("os.name").toLowerCase().contains("win")) {

            if(!Files.exists(Paths.get(testPath + "/mvnw.cmd"))) {
                throw new ErrorDuringEvaluationException("Error in finding maven executable");
            }
            mavenExecutable = new File(testPath + "/mvnw.cmd");

        } else {
            if(!Files.exists(Paths.get(testPath + "/mvnw"))) {
                throw new ErrorDuringEvaluationException("Error in finding maven executable");
            }
            mavenExecutable = new File(testPath + "/mvnw");
        }
        if(!mavenExecutable.setExecutable(true)){
            throw new ErrorDuringEvaluationException("Error in setting maven executable");
        }
        invoker.setMavenExecutable(mavenExecutable); //Set maven executable

        //Set logger
        CustomLogger customLogger = new CustomLogger();
        invoker.setLogger(customLogger);

        Float score = 0f;

        try {
            InvocationResult result = invoker.execute(request);

            MavenTestResultParser mavenTestResultParser = new MavenTestResultParser();

            try {
                score = mavenTestResultParser.parseTestResults(testPath + "/target/surefire-reports");
            }catch (Exception e) {
                System.err.println(customLogger.getOutput());
                throw new ErrorDuringEvaluationException("Error in parsing maven results");
            }

        } catch (MavenInvocationException e) {
            throw new ErrorDuringEvaluationException("Build failed.");
        }

        try {
            deleteFolder(tempFolderPath);
        } catch (IOException e) {
            //DO nothing
        }

        return score;
    }

    public Float scoreOfStaticAnalysis(SubmissionDTO submission) throws ErrorDuringEvaluationException {
        return new Random().nextFloat(0, 100);
    }
}
