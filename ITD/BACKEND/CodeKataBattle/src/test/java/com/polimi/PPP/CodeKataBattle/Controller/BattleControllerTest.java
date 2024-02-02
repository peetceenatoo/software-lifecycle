package com.polimi.PPP.CodeKataBattle.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleInviteRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import com.polimi.PPP.CodeKataBattle.service.BattleInviteService;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BattleControllerTest {


    @Autowired
    private BattleService battleService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private BattleInviteService battleInviteService;

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private BattleInviteRepository battleInviteRepository;

    @MockBean
    private GitHubAPI gitHubAPI;

    @MockBean
    private NotificationProvider notificationProvider;

    private String studentToken;

    private String student2Token;

    private String educatorToken;

    private BattleDTO battle;

    private BattleDTO battle2;

    private UserDTO student1;

    private UserDTO student2;

    private UserDTO educator;



    @BeforeAll
    void setUp() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(gitHubAPI.createRepository(anyString(), anyString(), anyBoolean())).thenReturn("repo/name");

        UserCreationDTO stud = new UserCreationDTO();
        stud.setUsername("stud");
        stud.setEmail("stu@gmail.com");
        stud.setPassword("Passowrd123!");
        stud.setName("NameUser1");
        stud.setSurname("SurnameUser1");
        stud.setRoleName("ROLE_STUDENT");
        stud.setLinkBio("linkBioUser1");

        UserCreationDTO stud2 = new UserCreationDTO();
        stud2.setUsername("stud2");
        stud2.setEmail("stu2@gmail.com");
        stud2.setPassword("Passowrd123!");
        stud2.setName("NameUser1");
        stud2.setSurname("SurnameUser1");
        stud2.setRoleName("ROLE_STUDENT");
        stud2.setLinkBio("linkBioUser1");

        this.student1 = userService.createUser(stud);
        this.student2 = userService.createUser(stud2);

        UserCreationDTO edu = new UserCreationDTO();
        edu.setUsername("edu");
        edu.setEmail("edu@gmail.com");
        edu.setPassword("Passowrd123!");
        edu.setName("NameUser1");
        edu.setSurname("SurnameUser1");
        edu.setRoleName("ROLE_EDUCATOR");
        edu.setLinkBio("linkBioUser2");


        this.educator = userService.createUser(edu);

        UserLoginDTO studLogin = new UserLoginDTO();
        studLogin.setEmail(stud.getEmail());
        studLogin.setPassword(stud.getPassword());

        UserLoginDTO stud2Login = new UserLoginDTO();
        stud2Login.setEmail(stud2.getEmail());
        stud2Login.setPassword(stud2.getPassword());

        UserLoginDTO eduLogin = new UserLoginDTO();
        eduLogin.setEmail(edu.getEmail());
        eduLogin.setPassword(edu.getPassword());

        final MockHttpServletResponse responseStudentLogin = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsBytes(studLogin)))
                .andReturn().getResponse();

        assertEquals(200, responseStudentLogin.getStatus());

        final MockHttpServletResponse responseStudent2Login = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsBytes(stud2Login)))
                .andReturn().getResponse();

        assertEquals(200, responseStudent2Login.getStatus());

        final MockHttpServletResponse responseEducatorLogin = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(eduLogin))).andReturn().getResponse();

        assertEquals(200, responseEducatorLogin.getStatus());

        UserLoggedDTO studentLogged = objectMapper.readValue(responseStudentLogin.getContentAsString(), UserLoggedDTO.class);
        UserLoggedDTO educatorLogged = objectMapper.readValue(responseEducatorLogin.getContentAsString(), UserLoggedDTO.class);
        UserLoggedDTO student2Logged = objectMapper.readValue(responseStudent2Login.getContentAsString(), UserLoggedDTO.class);

        this.studentToken = studentLogged.getToken();
        this.student2Token = student2Logged.getToken();
        this.educatorToken = educatorLogged.getToken();


        //Create tournament, enroll student and put it in ongoing stage

        //Create tournament

        TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
        tournamentCreationDTO.setEducatorsInvited(List.of(educator.getId()));
        tournamentCreationDTO.setTournamentName("TournamentName");
        tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now().plusDays(1));

        final MockHttpServletResponse responseCreateTournament = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/create")
                        .header("Authorization", "Bearer " + educatorToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(tournamentCreationDTO))).andReturn().getResponse();

        assertEquals(200, responseCreateTournament.getStatus());

        TournamentDTO tournament = objectMapper.readValue(responseCreateTournament.getContentAsString(), TournamentDTO.class);

        final MockHttpServletResponse responseEnrollTournament = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/"+ tournament.getId()+"/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();
        assertEquals(200, responseEnrollTournament.getStatus());


        final MockHttpServletResponse responseEnrollTournament2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/tournaments/"+ tournament.getId()+"/enroll")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")
                        ).andReturn().getResponse();
        assertEquals(200, responseEnrollTournament2.getStatus());

        tournamentService.updateStateForTournament(tournament.getId(), TournamentStateEnum.ONGOING);

        // Create a battle

        BattleCreationDTO battleCreationDTO = new BattleCreationDTO();
        battleCreationDTO.setName("BattleName");
        battleCreationDTO.setSubscriptionDeadline(ZonedDateTime.now().plusDays(1));
        battleCreationDTO.setSubmissionDeadline(ZonedDateTime.now().plusDays(2));
        battleCreationDTO.setMinStudentsInGroup(1);
        battleCreationDTO.setMaxStudentsInGroup(2);
        battleCreationDTO.setManualScoringRequired(false);
        battleCreationDTO.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);

        MockMultipartFile jsonFile = new MockMultipartFile("battle", "", "application/json", objectMapper.writeValueAsBytes(battleCreationDTO));


        final MockHttpServletResponse responseCreateBattle = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+tournament.getId()+"/createBattle")
                        .file(jsonFile)
                        .file(TournamentControllerTest.getGoodZip("codeZip"))
                        .file(TournamentControllerTest.getGoodZip("testZip"))
                        .header("Authorization", "Bearer " + educatorToken)
                        ).andReturn().getResponse();

        assertEquals(200, responseCreateBattle.getStatus());

        this.battle = objectMapper.readValue(responseCreateBattle.getContentAsString(), BattleDTO.class);

        battleCreationDTO.setName("BattleName2");
        MockMultipartFile jsonFile2 = new MockMultipartFile("battle", "", "application/json", objectMapper.writeValueAsBytes(battleCreationDTO));
        final MockHttpServletResponse responseCreateBattle2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/tournaments/"+tournament.getId()+"/createBattle")
                        .file(jsonFile2)
                        .file(TournamentControllerTest.getGoodZip("codeZip"))
                        .file(TournamentControllerTest.getGoodZip("testZip"))
                        .header("Authorization", "Bearer " + educatorToken)
                        ).andReturn().getResponse();

        assertEquals(200, responseCreateBattle2.getStatus());

        this.battle2 = objectMapper.readValue(responseCreateBattle2.getContentAsString(), BattleDTO.class);

        // Enroll stud1 inviting stud2
    }

    @Test
    @Transactional
    @Order(1)
    void testEnrollAndInvitation() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Battle battle = battleRepository.findById(this.battle.getId()).get();
        TournamentDTO tournament = tournamentService.getTournamentById(battle.getTournament().getId());

        // Getting original enrolled battles for students

        final MockHttpServletResponse responseOriginalEnrolled = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();
        assertEquals(200, responseOriginalEnrolled.getStatus());
        int originalEnrolled1 = objectMapper.readValue(responseOriginalEnrolled.getContentAsString(), List.class).size();

        final MockHttpServletResponse responseOriginalEnrolled2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseOriginalEnrolled2.getStatus());
        int originalEnrolled2 = objectMapper.readValue(responseOriginalEnrolled.getContentAsString(), List.class).size();

        // Enroll stud1, inviting stud2

        List<String> usernames =  List.of(student2.getUsername());

        final MockHttpServletResponse responseEnrollBattle = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/"+battle.getId()+"/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(usernames))).andReturn().getResponse();

        assertEquals(200, responseEnrollBattle.getStatus());

        // Enroll stud2, accepting invite
        BattleInvite battleInvite = battleInviteRepository.findByBattleIdAndInvitedUserId(battle.getId(), student2.getId()).get();

        String inviteToken = jwtHelper.generateInviteToken(battleInvite.getId(), battleInvite.getBattle().getSubscriptionDeadline());

        final MockHttpServletResponse responseEnrollBattle2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/acceptInvitation/"+inviteToken)
                        .contentType("application/json")
                        ).andReturn().getResponse();

        assertEquals(302, responseEnrollBattle2.getStatus());

        // Checking subscrption worked

        final MockHttpServletResponse responseEnrolled = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();
        assertEquals(200, responseEnrolled.getStatus());
        List<BattleStudentDTO> enrolledBattles = objectMapper.readValue(responseEnrolled.getContentAsString(), List.class);

        assertEquals(1, enrolledBattles.size() - originalEnrolled1);

        final MockHttpServletResponse responseEnrolled2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseEnrolled2.getStatus());
        List<BattleStudentDTO> enrolledBattles2 = objectMapper.readValue(responseEnrolled2.getContentAsString(), List.class);
        assertEquals(1, enrolledBattles2.size() - originalEnrolled2);

        // Enroll stud2 to battle2

        final MockHttpServletResponse responseEnroll2Battle2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/"+battle2.getId()+"/enroll")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(new ArrayList<>()))).andReturn().getResponse();

        assertEquals(200, responseEnroll2Battle2.getStatus());

        // Checking enrollment worked

        final MockHttpServletResponse responseEnrolled3 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")).andReturn().getResponse();

        List<BattleStudentDTO> enrolledBattles3 = objectMapper.readValue(responseEnrolled3.getContentAsString(), List.class);

        assertEquals(2, enrolledBattles3.size() - originalEnrolled2);

        // Trying to enroll stud2 to battle2 again

        final MockHttpServletResponse responseDoubleEnroll = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/"+battle2.getId()+"/enroll")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(new ArrayList<>()))).andReturn().getResponse();

        assertEquals(400, responseDoubleEnroll.getStatus());

        // Trying to invite stud2 to battle2, should fail

        final MockHttpServletResponse responseInvite = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/"+battle2.getId()+"/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(List.of(student2.getUsername())))).andReturn().getResponse();

        assertEquals(400, responseInvite.getStatus());

    }

    @Test
    @Transactional
    @Order(2)
    void mockGitHubAction() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Battle battle = battleRepository.findById(this.battle.getId()).get();
        TournamentDTO tournament = tournamentService.getTournamentById(battle.getTournament().getId());

        final MockHttpServletResponse responseGetEnrolled = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseGetEnrolled.getStatus());

        int originalEnrolled = objectMapper.readValue(responseGetEnrolled.getContentAsString(), List.class).size();

        final MockHttpServletResponse responseEnroll = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/"+battle.getId()+"/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(new ArrayList<>()))).andReturn().getResponse();

        assertEquals(200, responseEnroll.getStatus());

        final MockHttpServletResponse responseGetEnrolledAfter = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tournaments/"+tournament.getId()+"/battles/enrolled")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseGetEnrolledAfter.getStatus());

        int firstEnrolled = objectMapper.readValue(responseGetEnrolledAfter.getContentAsString(), List.class).size();

        assertEquals(1, firstEnrolled - originalEnrolled);

        battleService.changeBattleState(battle.getId(), BattleStateEnum.ONGOING);

        String invalidToken = "oefnodsfoidsfosdfnbodsi";

        CommitDTO commitDTO = new CommitDTO();
        commitDTO.setRepositoryUrl("repo/name");
        commitDTO.setCommitHash("hash");

        final MockHttpServletResponse responseGetToken = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId()+"/getGithubToken")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(commitDTO))).andReturn().getResponse();

        assertEquals(200, responseGetToken.getStatus());

        String token = objectMapper.readValue(responseGetToken.getContentAsString(), StudentGithubTokenDTO.class).getToken();

        assertTrue(jwtHelper.validateToken(token));

        final MockHttpServletResponse responseGetToken2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/commit")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(commitDTO))).andReturn().getResponse();

        assertEquals(200, responseGetToken2.getStatus());

        final MockHttpServletResponse responseGetTokenInvalid = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/commit")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(commitDTO))).andReturn().getResponse();

        assertEquals(401, responseGetTokenInvalid.getStatus());

        commitDTO.setCommitHash(null);

        final MockHttpServletResponse responseGetToken3 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/commit")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(commitDTO))).andReturn().getResponse();

        assertEquals(400, responseGetToken3.getStatus());

        commitDTO.setCommitHash("hash");
        commitDTO.setRepositoryUrl(null);

        final MockHttpServletResponse responseGetToken4 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/commit")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(commitDTO))).andReturn().getResponse();

        assertEquals(400, responseGetToken4.getStatus());

        // Enroll student to battle



    }

    @Test
    @Order(3)
    void testGetBattle() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        final MockHttpServletResponse responseGetBattle = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId())
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(400, responseGetBattle.getStatus());

        // Enroll student in battle

        final MockHttpServletResponse enrollStud = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/battles/"+battle.getId()+"/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(new ArrayList<>()))).andReturn().getResponse();

        assertEquals(200, enrollStud.getStatus());

        final MockHttpServletResponse responseGetBattleOk = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseGetBattleOk.getStatus());

        final MockHttpServletResponse responseGetBattle2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId())
                        .header("Authorization", "Bearer " + educatorToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseGetBattle2.getStatus());

        final MockHttpServletResponse responseGetBattle3 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId())
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(401, responseGetBattle3.getStatus());

        final MockHttpServletResponse responseGetBattle4 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/86686688668")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(400, responseGetBattle4.getStatus());

        final MockHttpServletResponse responseGetBattle5 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/86686688668")
                        .header("Authorization", "Bearer " + educatorToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(400, responseGetBattle5.getStatus());



    }

    @Test
    @Order(4)
    void testGetSubmissions() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        final MockHttpServletResponse responseGetBattle = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId())
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(400, responseGetBattle.getStatus());

        final MockHttpServletResponse responseGetBattleOk = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId()+"/submissions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseGetBattleOk.getStatus());

        final MockHttpServletResponse responseGetBattle2 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId() + "/submissions")
                        .header("Authorization", "Bearer " + educatorToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(200, responseGetBattle2.getStatus());

        final MockHttpServletResponse responseGetBattle3 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/"+battle.getId() +"/submissions")
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(401, responseGetBattle3.getStatus());

        final MockHttpServletResponse responseGetBattle4 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/86686688668/submissions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(400, responseGetBattle4.getStatus());

        final MockHttpServletResponse responseGetBattle5 = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/battles/86686688668/submissions")
                        .header("Authorization", "Bearer " + educatorToken)
                        .contentType("application/json")).andReturn().getResponse();

        assertEquals(400, responseGetBattle5.getStatus());



    }

    @AfterAll
    void tearDown() {
    }

}