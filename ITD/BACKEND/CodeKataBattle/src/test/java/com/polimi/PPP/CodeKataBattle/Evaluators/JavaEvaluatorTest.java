package com.polimi.PPP.CodeKataBattle.Evaluators;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.SubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.IGitHubAPI;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JavaEvaluatorTest {

    @Test
    void scoreOfFunctionalTests() {

        IGitHubAPI gitHubAPI = new GitHubAPI(true);
        Path tempDirectory;
        try{
            tempDirectory = Files.createTempDirectory("codekatabattle-evaluator");
        }catch (IOException ex){
            System.err.println("Error while creating temporary directory");
            throw new RuntimeException(ex);
        }
        String tempFolder = tempDirectory.toString();


        JavaEvaluator javaEvaluator = new JavaEvaluator(gitHubAPI, tempFolder);


        SubmissionDTO submissionDTO = new SubmissionDTO();
        BattleDTO battleDTO = new BattleDTO();
        battleDTO.setTestRepositoryLink("Jacopopiazza/TestRepo");
        submissionDTO.setBattle(battleDTO);
        submissionDTO.setRepositoryUrl("Jacopopiazza/RepoCode");
        submissionDTO.setCommitHash("076fe4d");

        Float score;

        try{
            score = javaEvaluator.scoreOfFunctionalTests(submissionDTO);
        }catch (Exception e){
            //Do nothing
            assertEquals(true, false);
            return;
        }

        Integer finalScore = (int)(100*score);
        System.out.println("Test result: " + finalScore);
        assertEquals(true, true);




    }

}