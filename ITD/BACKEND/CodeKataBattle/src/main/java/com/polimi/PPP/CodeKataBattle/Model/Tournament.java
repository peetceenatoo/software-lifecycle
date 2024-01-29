package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Deadline is mandatory")
    private LocalDateTime deadline;

    @Column(nullable = false, length = 256)
    @Enumerated(EnumType.STRING)
    private TournamentStateEnum state;

    @ManyToMany(mappedBy = "tournaments")
    private Set<User> users;

    @OneToMany(mappedBy = "tournament")
    private Set<Battle> battles;


}
