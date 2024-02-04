package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 256)
    private String surname;

    @Column(nullable = false, length = 256)
    private String password;

    @Column(nullable = false,unique = true, length = 256)
    @Email(message = "Email should be valid")
    private String email;

    @Column(nullable = false,unique = true, length = 50)
    private String username;

    @Column(name = "link_bio")
    private String linkBio;

    // Many users can have one role, hence ManyToOne relationship is used
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id") // role_id is the foreign key in the users table
    private Role role;

    @ManyToMany()
    @JoinTable(
            name = "users_tournaments",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tournament_id"))
    private Set<Tournament> tournaments;

}
