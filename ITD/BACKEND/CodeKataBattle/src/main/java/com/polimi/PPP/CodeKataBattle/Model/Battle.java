package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @NotBlank(message = "codeKataPath is mandatory")
    private String codeKataPath;

    @Column(nullable = false, length = 256)
    private String state;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Subsription Deadline is mandatory")
    private Date subscribtion_deadline;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Submission Deadline is mandatory")
    private Date submissiondeadline;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Max Students in Group is mandatory")
    private int maxStudentsInGroup;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Min Students in Group is mandatory")
    private int minStudentsInGroup;


    @Column(nullable = false)
    @NotBlank(message = "Manual Scoring option is mandatory")
    private boolean manualScoringRequires;

    @Column(length = 256)
    private String repositoryLink;

    @ManyToOne
    @JoinColumn(name="tournament_fk")
    private Tournament tournament;



}
