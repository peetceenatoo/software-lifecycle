package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;

public class BattleScore {
    @Id
    private Long id;

    private Long submissionId;

    @Column(nullable = false)
    @NotBlank(message = "Automatic score is mandatory")
    private int automaticScore;

    @Column(nullable = true)
    private int manualCorrection;

    @Lob
    @Column(nullable = true)
    private String logScoring;
}
