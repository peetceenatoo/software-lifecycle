package com.polimi.PPP.CodeKataBattle.Evaluators;

import com.polimi.PPP.CodeKataBattle.DTOs.SubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorDuringEvaluationException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidSubmissionStateException;
import com.polimi.PPP.CodeKataBattle.Model.ProgrammingLanguageEnum;
import com.polimi.PPP.CodeKataBattle.Model.SubmissionStateEnum;
import com.polimi.PPP.CodeKataBattle.Utilities.IGitHubAPI;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import com.polimi.PPP.CodeKataBattle.service.SubmissionService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.List;

@Service
@Slf4j
public class EvaluatorProcess {

    @Autowired
    private SubmissionService submissionService;
    private String tempFolder;

    @Autowired
    private IGitHubAPI gitHubAPI;

    @Async("taskExecutor")
    public void processSubmission(SubmissionDTO submissionDTO){
        //Process submission

        if(submissionDTO.getBattle().getProgrammingLanguage() != ProgrammingLanguageEnum.JAVA) {
            submissionService.createSubmissionScore(submissionDTO.getId(),
                    SubmissionStateEnum.FAILED,
                    0,
                    "Programming language not yet supported");
            return;
        }

        IEvaluator evaluator = new JavaEvaluator(gitHubAPI, tempFolder);

        Float functionalScore;
        try{
            functionalScore = 100 * evaluator.scoreOfFunctionalTests(submissionDTO);
        }catch (ErrorDuringEvaluationException ex){
            //TODO: Handle error, set submission status to error and post log

            try{
                submissionService.createSubmissionScore(submissionDTO.getId(),
                        SubmissionStateEnum.FAILED,
                        0,
                        "Log publishing yet to be implemented");
            }catch (InvalidSubmissionStateException e){
                // Do nothing
            }

            evaluator.cleanUp();
            return;
        }

        Float staticAnalysisScore;
        try{
            staticAnalysisScore = 100 * evaluator.scoreOfStaticAnalysis(submissionDTO);
        }catch (ErrorDuringEvaluationException ex){
            //TODO: Handle error, set submission status to error and post log
            try{
                submissionService.createSubmissionScore(submissionDTO.getId(),
                        SubmissionStateEnum.FAILED,
                        0,
                        "Log publishing yet to be implemented");
            }catch (InvalidSubmissionStateException e){
                // Do nothing
            }
            evaluator.cleanUp();
            return;
        }

        Timestamp submissionDeadlineTimestamp = Timestamp.valueOf(submissionDTO.getBattle().getSubmissionDeadline().toLocalDateTime());
        Timestamp subscriptionDeadlineTimestamp = Timestamp.valueOf(submissionDTO.getBattle().getSubscriptionDeadline().toLocalDateTime());
        Timestamp submissionTimestamp = Timestamp.valueOf(submissionDTO.getTimestamp().toLocalDateTime());

        Long epochSecondSubmission = submissionTimestamp.toInstant().atZone(ZoneId.of("UTC")).toEpochSecond();
        Long epochSecondSubmissionDeadline = submissionDeadlineTimestamp.toInstant().atZone(ZoneId.of("UTC")).toEpochSecond();
        Long epochSecondSubscriptionDeadline = subscriptionDeadlineTimestamp.toInstant().atZone(ZoneId.of("UTC")).toEpochSecond();

        Float timelinessScore = 100 * (1 - (float) (epochSecondSubmission - epochSecondSubscriptionDeadline) / (float) (epochSecondSubmissionDeadline - epochSecondSubscriptionDeadline));
        // 100 if submitted at subscriptionDeadline, 0 if submitted at submissionDeadline
        //Float timelinessScore = (1 - (float) (timestamp.getTime() - subscriptionDeadlineTimestamp.getTime()) / (float) (submissionDeadlineTimestamp.getTime() - subscriptionDeadlineTimestamp.getTime())) * 100;

        Float automaticScore = (functionalScore + timelinessScore + staticAnalysisScore) / 3;

        try{
            submissionService.createSubmissionScore(submissionDTO.getId(),
                    SubmissionStateEnum.COMPLETED,
                    automaticScore.intValue(),
                    "Log publishing yet to be implemented");
        }catch (InvalidSubmissionStateException e){
            // Do nothing
            submissionService.createSubmissionScore(submissionDTO.getId(),
                    SubmissionStateEnum.FAILED,
                    0,
                    "Log publishing yet to be implemented");
        }

        evaluator.cleanUp();

    }


    @PostConstruct
    public void init(){
        log.info("Evaluator process started");

        //this.tempFolder = this.tempFolder.replaceFirst("^~", System.getProperty("user.home"));
        Path tempDirectory;
        try{
            tempDirectory = Files.createTempDirectory("codekatabattle-evaluator");
        }catch (IOException ex){
            log.error("Error while creating temporary directory");
            throw new RuntimeException(ex);
        }
        this.tempFolder = tempDirectory.toString();



        //Checking if there are any pending submissions
        List<SubmissionDTO> pendingSubmissions = submissionService.getPendingSubmissions();

        log.info("Found " + pendingSubmissions.size() + " pending submissions");

        for(SubmissionDTO submissionDTO : pendingSubmissions){
            processSubmission(submissionDTO);
        }


    }

}
