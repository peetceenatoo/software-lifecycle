package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Custom query methods can go here
    Role findByName(String name);
}