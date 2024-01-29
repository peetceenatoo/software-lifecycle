package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Battles")
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(nullable = false, length = 256)
    @Enumerated(EnumType.STRING)
    private BattleStateEnum state;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Subsription Deadline is mandatory")
    private LocalDateTime subscriptionDeadline;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Submission Deadline is mandatory")
    private LocalDateTime submissionDeadline;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Max Students in Group is mandatory")
    private int maxStudentsInGroup;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Min Students in Group is mandatory")
    private int minStudentsInGroup;

    @Column(nullable = false)
    @NotBlank(message = "Manual Scoring option is mandatory")
    private boolean manualScoringRequired;

    @Column(length = 256)
    private String repositoryLink;

    @Column(length = 256)
    private String testRepositoryLink;

    @ManyToOne
    @JoinColumn(name="tournament_fk")
    private Tournament tournament;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProgrammingLanguageEnum programmingLanguage = ProgrammingLanguageEnum.JAVA;

}
