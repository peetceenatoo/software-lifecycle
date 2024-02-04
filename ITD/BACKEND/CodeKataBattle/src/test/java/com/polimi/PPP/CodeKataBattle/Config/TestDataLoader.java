package com.polimi.PPP.CodeKataBattle.Config;

import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Repositories.RoleRepository;

@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public TestDataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Prepopulate roles
        for (RoleEnum role : RoleEnum.values()) {
            if (!roleRepository.existsByName(role)) {
                Role newRole = new Role();
                newRole.setName(role);
                roleRepository.save(newRole);
            }
        }
        // Add other roles or data as necessary
    }
}

