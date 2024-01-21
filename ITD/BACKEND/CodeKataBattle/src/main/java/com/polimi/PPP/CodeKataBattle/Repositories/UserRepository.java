package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods can go here
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}