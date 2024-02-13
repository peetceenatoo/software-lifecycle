package com.polimi.PPP.CodeKataBattle.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ZonedDateTime timestamp;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubmissionStateEnum state;

    @Column(nullable = false)
    private String repositoryUrl;

    @Column(nullable = false)
    private String commitHash;

    @ManyToOne
    @JoinColumn(name="user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name="battle_fk")
    private Battle battle;
}
