package com.polimi.PPP.CodeKataBattle.Evaluators;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.SubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.IGitHubAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaEvaluatorTest {

    @Test
    void scoreOfFunctionalTests() {

        IGitHubAPI gitHubAPI = new GitHubAPI(true);
        String tempFolder = System.getProperty("java.io.tmpdir");
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