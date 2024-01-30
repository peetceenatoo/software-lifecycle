package com.polimi.PPP.CodeKataBattle.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime subscriptionDeadline;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime submissionDeadline;
    private int maxStudentsInGroup;
    private int minStudentsInGroup;
    private boolean manualScoringRequires;
    private ProgrammingLanguageEnum programmingLanguage;
}

