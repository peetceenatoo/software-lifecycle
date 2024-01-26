package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.UserCreationDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.EmailAlreadyExistsException;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testCreateUser() {
        UserCreationDTO newStudent = new UserCreationDTO(/* set user details */);

        newStudent.setEmail("first.user@email.com");
        newStudent.setUsername("firstuser");
        newStudent.setPassword("Password123!");
        newStudent.setName("First");
        newStudent.setSurname("User");
        newStudent.setRoleName("ROLE_STUDENT");
        newStudent.setLinkBio("https://www.linkedin.com/in/firstuser/");

        UserDTO createdUser = userService.createUser(newStudent);

        assertNotNull(createdUser);
        assertEquals(newStudent.getUsername(), createdUser.getUsername());
        assertEquals(newStudent.getEmail(), createdUser.getEmail());
        assertEquals(newStudent.getName(), createdUser.getName());
        assertEquals(newStudent.getSurname(), createdUser.getSurname());
        assertEquals(newStudent.getLinkBio(), createdUser.getLinkBio());
        assertEquals(RoleEnum.ROLE_STUDENT, createdUser.getRole().getName());

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(newStudent);
        });

        UserCreationDTO newEducator = new UserCreationDTO(/* set user details */);

        newEducator.setEmail("first.edu@email.com");
        newEducator.setUsername("firstedu");
        newEducator.setPassword("Password123!");
        newEducator.setName("First");
        newEducator.setSurname("User");
        newEducator.setRoleName("ROLE_EDUCATOR");
        newEducator.setLinkBio("https://www.linkedin.com/in/firstuser/");

        UserDTO createdEducator = userService.createUser(newEducator);

        assertNotNull(createdUser);
        assertEquals(newEducator.getUsername(), createdEducator.getUsername());
        assertEquals(newEducator.getEmail(), createdEducator.getEmail());
        assertEquals(newEducator.getName(), createdEducator.getName());
        assertEquals(newEducator.getSurname(), createdEducator.getSurname());
        assertEquals(newEducator.getLinkBio(), createdEducator.getLinkBio());
        assertEquals(RoleEnum.ROLE_EDUCATOR, createdEducator.getRole().getName());

    }

    // Additional test methods...
}
