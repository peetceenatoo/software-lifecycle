package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false,unique = true, length = 50)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "Description is mandatory")
    private String description;

    @Lob
    @Column(nullable = false)
    @NotBlank(message = "memo is mandatory")
    private String memo;

    @Column(nullable = false, length = 256)
    @NotBlank(message = "CodeLanguage is mandatory")
    private String codeLanguage;

    @ManyToOne
    @JoinColumn(name="tournament_fk")
    private Tournament tournament;


}
