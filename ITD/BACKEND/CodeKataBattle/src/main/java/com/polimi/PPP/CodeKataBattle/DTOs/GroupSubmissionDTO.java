package com.polimi.PPP.CodeKataBattle.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Setter
public class GroupSubmissionDTO {
    private Long submissionId;
    private ZonedDateTime submissionTimestamp;
    private String repositoryUrl;
    private String commitHash;
    private int automaticScore;
    private Integer manualCorrection; // Assuming it can be null
    private String logScoring;
    private Long groupId;

    public GroupSubmissionDTO(Long submissionId, ZonedDateTime submissionTimestamp, String repositoryUrl, String commitHash, int automaticScore, Integer manualCorrection, String logScoring, Long groupId) {
        this.submissionId = submissionId;
        this.submissionTimestamp = submissionTimestamp;
        this.repositoryUrl = repositoryUrl;
        this.commitHash = commitHash;
        this.automaticScore = automaticScore;
        this.manualCorrection = manualCorrection;
        this.logScoring = logScoring;
        this.groupId = groupId;
    }

}
