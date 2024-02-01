package com.polimi.PPP.CodeKataBattle.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polimi.PPP.CodeKataBattle.DTOs.UserCreationDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserLoginDTO;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.RoleRepository;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;



    @BeforeAll
    public void setUp() {

        UserCreationDTO user = new UserCreationDTO();
        user.setUsername("user1");
        user.setEmail("user1@gmail.com");
        user.setPassword("Passowrd123!");
        user.setName("NameUser1");
        user.setSurname("SurnameUser1");
        user.setRoleName("ROLE_STUDENT");
        user.setLinkBio("linkBioUser1");

        userService.createUser(user);



    }

    @Test
    void login() throws Exception {

        UserLoginDTO user = new UserLoginDTO();
        user.setEmail("user1@gmail.com");
        user.setPassword("Passowrd123!");

        UserLoginDTO user2 = new UserLoginDTO();
        user2.setEmail("user1@gmail.com");
        user2.setPassword("Passowrd1234!");

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonLogIn = objectMapper.writeValueAsString(user);

        final MockHttpServletResponse response = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                        .contentType("application/json")
                        .content(jsonLogIn))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        jsonLogIn = objectMapper.writeValueAsString(user2);

        final MockHttpServletResponse response2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                        .contentType("application/json")
                        .content(jsonLogIn))
                .andReturn().getResponse();

        assertEquals(401, response2.getStatus());

    }

    @Test
    void createUser() throws Exception {

        UserCreationDTO user = new UserCreationDTO();
        user.setUsername("user2");
        user.setEmail("test2@gmail.com");
        user.setPassword("Passowrd123!");
        user.setName("NameUser2");
        user.setSurname("SurnameUser2");
        user.setRoleName("ROLE_STUDENT");
        user.setLinkBio("linkBioUser2");

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonUser = objectMapper.writeValueAsString(user);

        final MockHttpServletResponse response = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/signup")
                        .contentType("application/json")
                        .content(jsonUser))
                .andReturn().getResponse();

        assertEquals(201, response.getStatus());

        UserCreationDTO user2 = new UserCreationDTO();
        user2.setUsername("user2");
        user2.setEmail("test2@gmail.com");
        user2.setPassword("Passowrd123!");
        user2.setName("NameUser2");
        user2.setSurname("SurnameUser2");
        user2.setRoleName("ROLE_STUDENT");
        user2.setLinkBio("linkBioUser2");

        jsonUser = objectMapper.writeValueAsString(user2);

        final MockHttpServletResponse response2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/signup")
                        .contentType("application/json")
                        .content(jsonUser))
                .andReturn().getResponse();

        assertEquals(400, response2.getStatus());

        user2.setUsername("user3");
        user2.setEmail(user.getEmail());

        jsonUser = objectMapper.writeValueAsString(user2);

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/signup")
                                .contentType("application/json")
                                .content(jsonUser))
                .andReturn().getResponse();

        assertEquals(400, response3.getStatus());

        UserCreationDTO user3 = new UserCreationDTO();
        user3.setUsername("user22323223");
        user3.setEmail("test232332@gmail.com");
        user3.setPassword("Passowrd123!");
        user3.setName("NameUser2");
        user3.setSurname("SurnameUser2");
        user3.setRoleName("ROLE_EDUCATOR");
        user3.setLinkBio("linkBioUser2");

        String jsonUser3 = objectMapper.writeValueAsString(user);

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/signup")
                                .contentType("application/json")
                                .content(jsonUser3))
                .andReturn().getResponse();

        assertEquals(201, response.getStatus());

    }



    @AfterEach
    void tearDown() {
    }
}