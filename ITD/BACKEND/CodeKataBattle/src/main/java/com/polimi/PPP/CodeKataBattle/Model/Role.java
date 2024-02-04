package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    // No need to define the inverse side of the relationship if it's not needed

}


