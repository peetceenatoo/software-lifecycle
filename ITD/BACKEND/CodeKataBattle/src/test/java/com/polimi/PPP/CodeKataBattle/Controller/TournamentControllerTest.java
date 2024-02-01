package com.polimi.PPP.CodeKataBattle.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.TournamentStateEnum;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TournamentService tournamentService;

    private String studentToken;

    private String educatorToken;

    private Integer createdTournaments = 0;

    @BeforeAll
    public void setUp() throws Exception {

        UserCreationDTO stud = new UserCreationDTO();
        stud.setUsername("stud");
        stud.setEmail("stu@gmail.com");
        stud.setPassword("Passowrd123!");
        stud.setName("NameUser1");
        stud.setSurname("SurnameUser1");
        stud.setRoleName("ROLE_STUDENT");
        stud.setLinkBio("linkBioUser1");

        userService.createUser(stud);

        UserCreationDTO edu = new UserCreationDTO();
        edu.setUsername("edu");
        edu.setEmail("edu@gmail.com");
        edu.setPassword("Passowrd123!");
        edu.setName("NameUser1");
        edu.setSurname("SurnameUser1");
        edu.setRoleName("ROLE_EDUCATOR");
        edu.setLinkBio("linkBioUser2");


        UserDTO educator = userService.createUser(edu);

        UserLoginDTO studLogin = new UserLoginDTO();
        studLogin.setEmail(stud.getEmail());
        studLogin.setPassword(stud.getPassword());

        UserLoginDTO eduLogin = new UserLoginDTO();
        eduLogin.setEmail(edu.getEmail());
        eduLogin.setPassword(edu.getPassword());

        ObjectMapper objectMapper = new ObjectMapper();

        final MockHttpServletResponse responseStudentLogin = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsBytes(studLogin)))
                .andReturn().getResponse();

        assertEquals(200, responseStudentLogin.getStatus());

        final MockHttpServletResponse responseEducatorLogin = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsBytes(eduLogin))).andReturn().getResponse();

        assertEquals(200, responseEducatorLogin.getStatus());

        UserLoggedDTO studentLogged = objectMapper.readValue(responseStudentLogin.getContentAsString(), UserLoggedDTO.class);
        UserLoggedDTO educatorLogged = objectMapper.readValue(responseEducatorLogin.getContentAsString(), UserLoggedDTO.class);

        this.studentToken = studentLogged.getToken();
        this.educatorToken = educatorLogged.getToken();

        TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
        tournamentCreationDTO.setTournamentName("Tournament1");
        tournamentCreationDTO.setEducatorsInvited(List.of(educator.getId()));
        tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(1));

        tournamentService.createTournament(tournamentCreationDTO);

        createdTournaments += 1;


    }
    @Test
    @Order(1)
    void getTournament() throws Exception {

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response2.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments")
                                .contentType("application/json"))
                        .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();


        List<TournamentDTO> tournaments = objectMapper.readValue(response.getContentAsString(), List.class);

        assertEquals(1, tournaments.size());


    }
    @Test
    @Order(2)
    void createTournament() throws Exception{

        TournamentCreationDTO failedTournamentCreationDTO = new TournamentCreationDTO();
        failedTournamentCreationDTO.setTournamentName("Tournament2");
        failedTournamentCreationDTO.setEducatorsInvited(List.of(1L,2L,3L,4L));
        failedTournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(1));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();


        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/create")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken)
                                .content(objectMapper.writeValueAsBytes(failedTournamentCreationDTO)))
                .andReturn().getResponse();

        assertEquals(400, response.getStatus());

        TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
        tournamentCreationDTO.setTournamentName("Tournament2");
        tournamentCreationDTO.setEducatorsInvited(List.of(2L));
        tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(-1));

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/create")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken)
                                .content(objectMapper.writeValueAsBytes(tournamentCreationDTO)))
                .andReturn().getResponse();

        assertEquals(200, response3.getStatus());
        createdTournaments += 1;


        tournamentCreationDTO.setTournamentName("Tournament3456");

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/create")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken)
                                .content(objectMapper.writeValueAsBytes(tournamentCreationDTO)))
                .andReturn().getResponse();

        assertEquals(401, response4.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/create")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsBytes(tournamentCreationDTO)))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        TournamentDTO tournamentDTO = objectMapper.readValue(response3.getContentAsString(), TournamentDTO.class);

    }

    @Test
    @Order(3)
    void getManagedTournaments() throws Exception{

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/managed")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/managed")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        final MockHttpServletResponse responseStudent = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/managed")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(401, responseStudent.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();

        List<TournamentDTO> tournaments = objectMapper.readValue(response.getContentAsString(), List.class);

        assertEquals(2, tournaments.size());

    }

    @Test
    @Order(4)
    void getEnrolledTournaments() throws Exception{

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/enrolled")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/enrolled")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        final MockHttpServletResponse responseEducator = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/enrolled")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(401, responseEducator.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();

        List<TournamentDTO> tournaments = objectMapper.readValue(response.getContentAsString(), List.class);

        assertEquals(0, tournaments.size());

    }

    @Test
    @Order(5)
    void enrollInTournament() throws Exception {

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/enrolled")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        int initial = objectMapper.readValue(response.getContentAsString(), List.class).size();

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/1/enroll")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response2.getStatus());

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/enrolled")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response3.getStatus());

        int finalSize = objectMapper.readValue(response3.getContentAsString(), List.class).size();

        assertEquals(initial + 1, finalSize);

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/1/enroll")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(400, response4.getStatus());

        final MockHttpServletResponse response5 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/1/enroll")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(401, response5.getStatus());

        final MockHttpServletResponse response6 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/1/enroll")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, response6.getStatus());

    }

    @Test
    @Order(6)
    void getTournamentById() throws Exception {

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/1")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/1")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response2.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/1")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        TournamentDTO tournamentDTO = objectMapper.readValue(response.getContentAsString(), TournamentDTO.class);

        assertEquals("Tournament1", tournamentDTO.getName());

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/-1")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(400, response3.getStatus());


    }

    @Test
    @Order(7)
    void getTournamentByState() throws Exception{

        int total = tournamentService.getTournaments(null).size();
        int sub = tournamentService.getTournaments(TournamentStateEnum.SUBSCRIPTION).size();
        int ong = tournamentService.getTournaments(TournamentStateEnum.ONGOING).size();
        int end = tournamentService.getTournaments(TournamentStateEnum.ENDED).size();

        final MockHttpServletResponse responseEdu = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/SUBSCRIPTION")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, responseEdu.getStatus());

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/SUBSCRIPTION")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        int retrivedSub = objectMapper.readValue(response.getContentAsString(), List.class).size();
        assertEquals(sub, retrivedSub);

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/ONGOING")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response2.getStatus());
        int retrivedOng = objectMapper.readValue(response2.getContentAsString(), List.class).size();
        assertEquals(ong, retrivedOng);

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/ENDED")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response3.getStatus());
        int retrivedEnd = objectMapper.readValue(response3.getContentAsString(), List.class).size();
        assertEquals(end, retrivedEnd);

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/HFHFHFHF")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(400, response4.getStatus());

        final MockHttpServletResponse response5 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(404, response5.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/state/SUBSCRIPTION")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

    }

    @Test
    @Order(8)
    void searchByKeyword() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/search/ourn")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        List<TournamentDTO> tournaments = objectMapper.readValue(response.getContentAsString(), List.class);

        assertEquals(createdTournaments, tournaments.size());

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/search/ourn")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response2.getStatus());

        List<TournamentDTO> tournaments2 = objectMapper.readValue(response2.getContentAsString(), List.class);

        assertEquals(createdTournaments, tournaments2.size());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/search/ourn")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/search/voihvsdoifhosdihf")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response3.getStatus());

        List<TournamentDTO> tournaments3 = objectMapper.readValue(response3.getContentAsString(), List.class);

        assertEquals(0, tournaments3.size());

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/search/1")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response4.getStatus());

        List<TournamentDTO> tournaments4 = objectMapper.readValue(response4.getContentAsString(), List.class);

        assertEquals(1, tournaments4.size());

    }


}