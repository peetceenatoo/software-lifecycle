package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import com.polimi.PPP.CodeKataBattle.Model.ProgrammingLanguageEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class BattleDTO {
    private Long id;
    private String name;
    private BattleStateEnum state;
    private ZonedDateTime subscriptionDeadline;
    private ZonedDateTime submissionDeadline;
    private int maxStudentsInGroup;
    private int minStudentsInGroup;
    private boolean manualScoringRequired;
    private String repositoryLink;
    private String testRepositoryLink;
    private ProgrammingLanguageEnum programmingLanguage;
    // Getters and setters
}
