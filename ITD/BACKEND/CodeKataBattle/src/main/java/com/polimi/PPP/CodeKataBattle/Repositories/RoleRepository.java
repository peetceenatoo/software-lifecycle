package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnunm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Custom query methods can go here
    Optional<Role> findByName(RoleEnunm name);

    Boolean existsByName(RoleEnunm name);
}