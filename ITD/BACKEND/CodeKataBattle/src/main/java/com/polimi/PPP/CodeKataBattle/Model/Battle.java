package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
    private String name;

    @Column(nullable = false, length = 256)
    @Enumerated(EnumType.STRING)
    private BattleStateEnum state;

    @Column(nullable = false, length = 256)
    private ZonedDateTime subscriptionDeadline;

    @Column(nullable = false, length = 256)
    private ZonedDateTime submissionDeadline;

    @Column(nullable = false, length = 256)
    private int maxStudentsInGroup;

    @Column(nullable = false, length = 256)
    private int minStudentsInGroup;

    @Column(nullable = false)
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
