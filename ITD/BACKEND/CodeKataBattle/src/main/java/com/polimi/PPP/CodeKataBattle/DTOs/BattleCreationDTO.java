package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.ProgrammingLanguageEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BattleCreationDTO {
    private String name;
    private LocalDateTime subscriptionDeadline;
    private LocalDateTime submissionDeadline;
    private int maxStudentsInGroup;
    private int minStudentsInGroup;
    private boolean manualScoringRequires;
    private ProgrammingLanguageEnum programmingLanguage;
}

