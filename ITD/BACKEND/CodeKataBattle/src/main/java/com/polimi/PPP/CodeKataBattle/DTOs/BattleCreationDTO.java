package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Annotations.ValidTimezone;
import com.polimi.PPP.CodeKataBattle.Model.ProgrammingLanguageEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BattleCreationDTO {
    private String name;
    private ZonedDateTime subscriptionDeadline;
    private ZonedDateTime submissionDeadline;
    private int maxStudentsInGroup;
    private int minStudentsInGroup;
    private boolean manualScoringRequires;
    private ProgrammingLanguageEnum programmingLanguage;
    @NotNull
    @ValidTimezone
    private String timeZone;
}

