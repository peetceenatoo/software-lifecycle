package com.polimi.PPP.CodeKataBattle.Config;

import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnunm;
import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.RoleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // Check if the user already exists to avoid duplicates
            if (userRepository.findByUsername("testuser").isEmpty()) {
                // Create a new role or fetch an existing one
                Role userRole = roleRepository.findByName(RoleEnunm.ROLE_EDUCATOR);
                if (userRole == null) {
                    userRole = new Role();
                    userRole.setName(RoleEnunm.ROLE_EDUCATOR);
                    roleRepository.save(userRole);
                }

                // Create a new user
                User fakeUser = new User();
                fakeUser.setName("John");
                fakeUser.setSurname("Doe");
                // fakeUser.setPassword("password"); // Ideally, you should encrypt the password
                fakeUser.setPassword(passwordEncoder.encode("password"));
                fakeUser.setEmail("john.doe@example.com");
                fakeUser.setUsername("testuser");
                fakeUser.setLinkBio("http://example.com/bio");
                fakeUser.setRole(userRole);

                userRepository.save(fakeUser);
            }
        };
    }
}

