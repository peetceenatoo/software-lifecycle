package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BattleScores")
public class BattleScore {
    @Id
    private Long id;

    @Column(nullable = false)
    private int automaticScore;

    @Column(nullable = true)
    private int manualCorrection;

    @Lob
    @Column(nullable = true)
    private String logScoring;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_fk")
    private Submission submission;


}
