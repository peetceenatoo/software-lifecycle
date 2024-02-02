package com.polimi.PPP.CodeKataBattle.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.ProgrammingLanguageEnum;
import com.polimi.PPP.CodeKataBattle.Model.Tournament;
import com.polimi.PPP.CodeKataBattle.Model.TournamentStateEnum;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


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

    @Autowired
    private BattleService battleService;

    private String studentToken;

    private String educatorToken;

    @MockBean
    private GitHubAPI gitHubAPI;
    @MockBean
    @Qualifier("emailProvider")
    private NotificationProvider notificationProvider;

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
        tournamentCreationDTO.setEducatorsInvited(new ArrayList<>());
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

    @Test
    @Order(9)
    void testCreateBattle() throws Exception{
        when(gitHubAPI.createRepository(anyString(), anyString(),anyBoolean())).thenReturn("name/repo");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        BattleCreationDTO battleCreationDTO = new BattleCreationDTO();
        battleCreationDTO.setName("Battle1");
        battleCreationDTO.setManualScoringRequired(false);
        battleCreationDTO.setSubmissionDeadline(ZonedDateTime.now().plusDays(4));
        battleCreationDTO.setSubscriptionDeadline(ZonedDateTime.now().plusDays(3));
        battleCreationDTO.setMinStudentsInGroup(1);
        battleCreationDTO.setMaxStudentsInGroup(2);
        battleCreationDTO.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);

        MockMultipartFile jsonFile = new MockMultipartFile("battle", "", "application/json", objectMapper.writeValueAsBytes(battleCreationDTO));


        TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
        tournamentCreationDTO.setTournamentName("TournamentSub");
        tournamentCreationDTO.setEducatorsInvited(List.of(2L));
        tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(-1));

        TournamentDTO tournament = tournamentService.createTournament(tournamentCreationDTO);


        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+ tournament.getId() +"/createBattle")
                                .file(jsonFile)
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+ tournament.getId() +"/createBattle")
                                .file(jsonFile)
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.studentToken)

                )

                .andReturn().getResponse();

        assertEquals(401, response2.getStatus());

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+ tournament.getId() +"/createBattle")
                                .file(jsonFile)
                                .file(getGoodZip("codeZippp"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response3.getStatus());

        final MockHttpServletResponse response8 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+ tournament.getId() +"/createBattle")
                                .file(jsonFile)
                                .file(getBadZip1("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response8.getStatus());

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+ tournament.getId() +"/createBattle")
                                .file(jsonFile)
                                .file(getGoodZip("codeZip"))
                                .file(getBadZip1("testZippppppp"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response4.getStatus());

        final MockHttpServletResponse response5 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/7757577575/createBattle")
                                .file(jsonFile)
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response5.getStatus());

        final MockHttpServletResponse response6 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+tournament.getId()+"/createBattle")
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response6.getStatus());


        BattleCreationDTO battleCreationDTO2 = new BattleCreationDTO();
        battleCreationDTO2.setName("Battle1");
        battleCreationDTO2.setManualScoringRequired(false);
        battleCreationDTO2.setSubmissionDeadline(ZonedDateTime.now().plusDays(4));
        battleCreationDTO2.setSubscriptionDeadline(ZonedDateTime.now().plusDays(5));
        battleCreationDTO2.setMinStudentsInGroup(1);
        battleCreationDTO2.setMaxStudentsInGroup(2);
        battleCreationDTO2.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);

        MockMultipartFile jsonFile2 = new MockMultipartFile("battle", "", "application/json", objectMapper.writeValueAsBytes(battleCreationDTO));

        final MockHttpServletResponse response7 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+tournament.getId()+"/createBattle")
                                .file(jsonFile2)
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response7.getStatus());

        battleCreationDTO2.setSubscriptionDeadline(ZonedDateTime.now().plusDays(3));
        battleCreationDTO2.setMinStudentsInGroup(10);
        battleCreationDTO2.setMaxStudentsInGroup(2);

        final MockHttpServletResponse response9 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+tournament.getId()+"/createBattle")
                                .file(jsonFile2)
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response9.getStatus());

        battleCreationDTO2.setMinStudentsInGroup(1);
        battleCreationDTO2.setMaxStudentsInGroup(2);
        battleCreationDTO2.setName(null);

        final MockHttpServletResponse response10 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+tournament.getId()+"/createBattle")
                                .file(jsonFile2)
                                .file(getGoodZip("codeZip"))
                                .file(getGoodZip("testZip"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", "Bearer " + this.educatorToken)

                )

                .andReturn().getResponse();

        assertEquals(400, response10.getStatus());


    }

    @Test
    @Order(10)
    void getTournamentBattles() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(gitHubAPI.createRepository(anyString(), anyString(),anyBoolean())).thenReturn("name/repo");


        List<TournamentDTO> tournaments = tournamentService.searchTournamentsByKeyword("TournamentSub");
        long tournamentId = -1;
        if(tournaments.isEmpty()){
            TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
            tournamentCreationDTO.setTournamentName("TournamentSub");
            tournamentCreationDTO.setEducatorsInvited(List.of(2L));
            tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(-1));

            TournamentDTO tournament = tournamentService.createTournament(tournamentCreationDTO);
            tournamentId = tournament.getId();

            tournamentService.updateStateForTournament(tournamentId, TournamentStateEnum.ONGOING);


            BattleCreationDTO battleCreationDTO = new BattleCreationDTO();
            battleCreationDTO.setName("BattleNewForTest");
            battleCreationDTO.setManualScoringRequired(false);
            battleCreationDTO.setSubmissionDeadline(ZonedDateTime.now().plusDays(4));
            battleCreationDTO.setSubscriptionDeadline(ZonedDateTime.now().plusDays(3));
            battleCreationDTO.setMinStudentsInGroup(1);
            battleCreationDTO.setMaxStudentsInGroup(2);
            battleCreationDTO.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);

            //Mock status of the tournament to ongoing

            BattleDTO battle = battleService.createBattle(tournamentId, battleCreationDTO, getGoodZip("codeZip"), getGoodZip("testZip"));



        }
        else{
            tournamentId = tournaments.get(0).getId();
            if(tournaments.get(0).getState() == TournamentStateEnum.SUBSCRIPTION)
                tournamentService.updateStateForTournament(tournamentId, TournamentStateEnum.ONGOING);

        }

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournamentId+"/battles")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        List<BattleDTO> battles = objectMapper.readValue(response.getContentAsString(), List.class);

        assertEquals(1, battles.size());

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournamentId+"/battles")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response2.getStatus());

        List<BattleDTO> battles2 = objectMapper.readValue(response2.getContentAsString(), List.class);

        assertEquals(1, battles2.size());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournamentId+"/battles")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/7757577575/battles")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(400, response3.getStatus());




    }

    @Test
    @Order(11)
    void testCloseTournament() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(gitHubAPI.createRepository(anyString(), anyString(),anyBoolean())).thenReturn("name/repo");


        List<TournamentDTO> tournaments = tournamentService.searchTournamentsByKeyword("TournamentSub");
        long tournamentId = -1;
        if(tournaments.isEmpty()){
            TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
            tournamentCreationDTO.setTournamentName("TournamentSub");
            tournamentCreationDTO.setEducatorsInvited(List.of(2L));
            tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(-1));

            TournamentDTO tournament = tournamentService.createTournament(tournamentCreationDTO);
            tournamentId = tournament.getId();

            tournamentService.updateStateForTournament(tournamentId, TournamentStateEnum.ONGOING);

            BattleCreationDTO battleCreationDTO = new BattleCreationDTO();
            battleCreationDTO.setName("BattleNewForTest");
            battleCreationDTO.setManualScoringRequired(false);
            battleCreationDTO.setSubmissionDeadline(ZonedDateTime.now().plusDays(4));
            battleCreationDTO.setSubscriptionDeadline(ZonedDateTime.now().plusDays(3));
            battleCreationDTO.setMinStudentsInGroup(1);
            battleCreationDTO.setMaxStudentsInGroup(2);
            battleCreationDTO.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);

            //Mock status of the tournament to ongoing

            BattleDTO battle = battleService.createBattle(tournamentId, battleCreationDTO, getGoodZip("codeZip"), getGoodZip("testZip"));

        }
        else{
            tournamentId = tournaments.get(0).getId();
            if(tournaments.get(0).getState() == TournamentStateEnum.SUBSCRIPTION)
                tournamentService.updateStateForTournament(tournamentId, TournamentStateEnum.ONGOING);

        }

        final MockHttpServletResponse response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/"+tournamentId+"/close")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.studentToken))
                .andReturn().getResponse();

        assertEquals(401, response.getStatus());

        final MockHttpServletResponse response2 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/"+tournamentId+"/close")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        // Ongoing battle
        assertEquals(400, response2.getStatus());

        TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
        tournamentCreationDTO.setTournamentName("TournamentSub2");
        tournamentCreationDTO.setEducatorsInvited(List.of(2L));
        tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(-1));

        TournamentDTO tournament = tournamentService.createTournament(tournamentCreationDTO);
        tournamentId = tournament.getId();

        final MockHttpServletResponse response3 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/"+tournamentId+"/close")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(200, response3.getStatus());

        final MockHttpServletResponse response4 = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/86868686868/close")
                                .contentType("application/json")
                                .header("Authorization", "Bearer " + this.educatorToken))
                .andReturn().getResponse();

        assertEquals(400, response4.getStatus());

        final MockHttpServletResponse responseNoAuth = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/86868686868/close")
                                .contentType("application/json"))
                .andReturn().getResponse();

        assertEquals(401, responseNoAuth.getStatus());

    }

    public static MockMultipartFile getGoodZip(String name) throws IOException {

        MockMultipartFile mockMultipartFile;

        try {
            File zipFile = createGoodTempZipFile();
            byte[] zipContent = readFileToByteArray(zipFile);

            mockMultipartFile = new MockMultipartFile(
                    name, // Parameter name for the multipart file
                    zipFile.getName(), // Filename
                    "application/zip", // Content type
                    zipContent // File content
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return mockMultipartFile;
    }

    public static MockMultipartFile getBadZip1(String name) throws IOException{
        //Mock zip files
        MockMultipartFile mockMultipartFile;

        try {
            File zipFile = createBadTempZipFile1();
            byte[] zipContent = readFileToByteArray(zipFile);

            mockMultipartFile = new MockMultipartFile(
                    name, // Parameter name for the multipart file
                    zipFile.getName(), // Filename
                    "application/zip", // Content type
                    zipContent // File content
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return mockMultipartFile;
    }

    private static File createGoodTempZipFile() throws IOException {

        // Temp dir
        Path tempDir = Files.createTempDirectory("myTempDir");


        // Temporary file
        File tempZipFile = new File(tempDir.toFile(), "temp.zip");

        try (FileOutputStream fos = new FileOutputStream(tempZipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Adding pom.xml file
            ZipEntry pomEntry = new ZipEntry("pom.xml");
            zos.putNextEntry(pomEntry);
            String pomContent = "<project>...</project>"; // Replace with actual pom.xml content
            zos.write(pomContent.getBytes());
            zos.closeEntry();

            // Adding mvnw file
            ZipEntry mvnwEntry = new ZipEntry("mvnw");
            zos.putNextEntry(mvnwEntry);
            String mvnwContent = "<project>...</project>"; // Replace with actual mvnw.xml content
            zos.write(mvnwContent.getBytes());
            zos.closeEntry();

            // Adding src directory
            ZipEntry srcDirEntry = new ZipEntry("src/");
            zos.putNextEntry(srcDirEntry);
            zos.closeEntry();

            // You can add more files or subdirectories inside 'src' if needed
            // Example: Adding a file inside src directory
            // ZipEntry srcFileEntry = new ZipEntry("src/MyClass.java");
            // zos.putNextEntry(srcFileEntry);
            // String srcFileContent = "public class MyClass {}";
            // zos.write(srcFileContent.getBytes());
            // zos.closeEntry();
        }

        return tempZipFile;
    }

    private static File createBadTempZipFile1() throws IOException {

        // Temp dir
        Path tempDir = Files.createTempDirectory("myTempDir");


        // Temporary file
        File tempZipFile = new File(tempDir.toFile(), "temp.zip");

        try (FileOutputStream fos = new FileOutputStream(tempZipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Adding pom.xml file
            ZipEntry pomEntry = new ZipEntry("poma.xml");
            zos.putNextEntry(pomEntry);
            String pomContent = "<project>...</project>"; // Replace with actual pom.xml content
            zos.write(pomContent.getBytes());
            zos.closeEntry();

            // Adding src directory
            ZipEntry srcDirEntry = new ZipEntry("src/");
            zos.putNextEntry(srcDirEntry);
            zos.closeEntry();

            // You can add more files or subdirectories inside 'src' if needed
            // Example: Adding a file inside src directory
            // ZipEntry srcFileEntry = new ZipEntry("src/MyClass.java");
            // zos.putNextEntry(srcFileEntry);
            // String srcFileContent = "public class MyClass {}";
            // zos.write(srcFileContent.getBytes());
            // zos.closeEntry();
        }

        return tempZipFile;
    }

    private static  byte[] readFileToByteArray(File file) throws IOException{
        return Files.readAllBytes(file.toPath());
    }


}