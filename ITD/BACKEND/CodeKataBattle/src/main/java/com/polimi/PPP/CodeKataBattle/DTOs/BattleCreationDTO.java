package com.polimi.PPP.CodeKataBattle.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.polimi.PPP.CodeKataBattle.Model.ProgrammingLanguageEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BattleCreationDTO {
    @NotBlank(message = "Battle name is mandatory")
    private String name;
    @NotNull(message = "Subscription deadline is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime subscriptionDeadline;
    @NotNull(message = "Submission deadline is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime submissionDeadline;

    @NotNull(message = "maxStudentsInGroup is mandatory")
    @Positive(message = "maxStudentsInGroup must be positive")
    private int maxStudentsInGroup;

    @NotNull(message = "minStudentsInGroup is mandatory")
    @Positive(message = "minStudentsInGroup must be positive")
    private int minStudentsInGroup;

    @NotNull(message = "manualScoringRequired is mandatory")
    private boolean manualScoringRequired;

    @NotBlank(message = "programmingLanguage is mandatory")
    private ProgrammingLanguageEnum programmingLanguage;
}

