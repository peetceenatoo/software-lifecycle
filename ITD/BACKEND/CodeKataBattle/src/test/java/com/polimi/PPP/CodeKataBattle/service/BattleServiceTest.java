package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleCreationException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleStateException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class BattleServiceTest {

    private BattleService battleService;

    private UserDTO student;

    private TournamentDTO tournamentDTO;

    private MockMvc mockMvc;

    private UserDTO educator;

    @Mock
    private BattleRepository battleRepository;

    @Mock
    private BattleScoreRepository battleScoreRepository;

    @Mock
    private BattleInviteRepository battleInviteRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Mock
    private BattleSubscriptionRepository battleSubscriptionRepository;

    @Mock
    private UserRepository userRepository;


    @Mock
    private GitHubAPI gitHubAPI;

    @Mock
    private ApplicationEventPublisher eventPublisher;




    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        this.battleService = new BattleService(battleRepository, battleScoreRepository, tournamentRepository, gitHubAPI, modelMapper, battleSubscriptionRepository
                ,userRepository, battleInviteRepository, eventPublisher);
    }
    
    @Test
    public void testChangeBattleState() {

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("NOME");
        tournament.setState(TournamentStateEnum.ONGOING);
        tournament.setDeadline(LocalDateTime.now());
        tournament.setBattles(new HashSet<>());
        tournament.setUsers(new HashSet<>());


        Battle battle = new Battle();
        battle.setState(BattleStateEnum.SUBSCRIPTION);
        battle.setTournament(tournament);
        battle.setId(1L);
        battle.setManualScoringRequired(true);
        battle.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);
        battle.setRepositoryLink("repoLink");
        battle.setTestRepositoryLink("testLink");
        battle.setMinStudentsInGroup(1);
        battle.setMaxStudentsInGroup(3);
        battle.setSubmissionDeadline(LocalDateTime.now());
        battle.setSubscriptionDeadline(LocalDateTime.now());
        battle.setName("Battle");

        BattleDTO battleDTO = new BattleDTO();
        modelMapper.map(battle, battleDTO);

        BattleDTO battleUpdated = new BattleDTO();
        Battle battleUpdatedEntity = new Battle();
        modelMapper.map(battle, battleUpdatedEntity);
        modelMapper.map(battle, battleUpdated);
        battleUpdatedEntity.setState(BattleStateEnum.ONGOING);
        battleUpdated.setState(BattleStateEnum.ONGOING);

        when(battleRepository.findById(1L)).thenReturn(java.util.Optional.of(battle));
        when(battleRepository.save(any(Battle.class))).thenReturn(battleUpdatedEntity);

        Battle found = battleRepository.findById(1L).get();
        checkBattlesAreEquals(battle, found);

        BattleDTO updated = battleService.changeBattleState(1L, BattleStateEnum.ONGOING);
        checkBattlesAreEquals(battleUpdated, updated);

        when(battleRepository.findById(1L)).thenReturn(java.util.Optional.of(battleUpdatedEntity));

        assertThrows(InvalidArgumentException.class,() -> {
            battleService.changeBattleState(1L, BattleStateEnum.ONGOING);
        });

    }

    private void checkBattlesAreEquals(Battle battle1, Battle battle2){
        assertEquals(battle1.getId(), battle2.getId());
        assertEquals(battle1.getName(), battle2.getName());
        assertEquals(battle1.getState(), battle2.getState());
        assertEquals(battle1.getRepositoryLink(), battle2.getRepositoryLink());
        assertEquals(battle1.getTestRepositoryLink(), battle2.getTestRepositoryLink());
        assertEquals(battle1.isManualScoringRequired(), battle2.isManualScoringRequired());
        assertEquals(battle1.getProgrammingLanguage(), battle2.getProgrammingLanguage());
        assertEquals(battle1.getSubmissionDeadline(), battle2.getSubmissionDeadline());
        assertEquals(battle1.getSubscriptionDeadline(), battle2.getSubscriptionDeadline());
        assertEquals(battle1.getMinStudentsInGroup(), battle2.getMinStudentsInGroup());
        assertEquals(battle1.getMaxStudentsInGroup(), battle2.getMaxStudentsInGroup());
    }

    private void checkBattlesAreEquals(BattleDTO battle1, BattleDTO battle2){
        assertEquals(battle1.getId(), battle2.getId());
        assertEquals(battle1.getName(), battle2.getName());
        assertEquals(battle1.getState(), battle2.getState());
        assertEquals(battle1.getRepositoryLink(), battle2.getRepositoryLink());
        assertEquals(battle1.getTestRepositoryLink(), battle2.getTestRepositoryLink());
        assertEquals(battle1.isManualScoringRequired(), battle2.isManualScoringRequired());
        assertEquals(battle1.getProgrammingLanguage(), battle2.getProgrammingLanguage());
        assertEquals(battle1.getSubmissionDeadline(), battle2.getSubmissionDeadline());
        assertEquals(battle1.getSubscriptionDeadline(), battle2.getSubscriptionDeadline());
        assertEquals(battle1.getMinStudentsInGroup(), battle2.getMinStudentsInGroup());
        assertEquals(battle1.getMaxStudentsInGroup(), battle2.getMaxStudentsInGroup());
    }



    @Test
    public void createBattle() throws IOException {

        Long tournamentId = 10L;

        LocalDateTime deadlines = LocalDateTime.now();

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("NOME");
        tournament.setState(TournamentStateEnum.ONGOING);
        tournament.setDeadline(deadlines);
        tournament.setBattles(new HashSet<>());
        tournament.setUsers(new HashSet<>());
        
        BattleDTO mockBattle = new BattleDTO();
        //SetUp
        mockBattle.setId(1L);
        mockBattle.setName("Battle");
        mockBattle.setRepositoryLink("repoLink");
        mockBattle.setState(BattleStateEnum.ONGOING);
        mockBattle.setMinStudentsInGroup(1);
        mockBattle.setMaxStudentsInGroup(3);
        mockBattle.setTestRepositoryLink("TestLink");
        mockBattle.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);
        mockBattle.setManualScoringRequired(true);
        mockBattle.setSubmissionDeadline(deadlines);
        mockBattle.setSubscriptionDeadline(deadlines);

        Battle mockBattleEntity = new Battle();
        modelMapper.map(mockBattle, mockBattleEntity);
        mockBattleEntity.setTournament(tournament);


        // Mock save
        MockMultipartFile mockZip = getGoodZip();

        BattleCreationDTO battleCreationDTO = new BattleCreationDTO();
        battleCreationDTO.setName("Battle");
        battleCreationDTO.setManualScoringRequires(true);
        battleCreationDTO.setProgrammingLanguage(ProgrammingLanguageEnum.JAVA);
        battleCreationDTO.setSubmissionDeadline(deadlines);
        battleCreationDTO.setSubscriptionDeadline(deadlines);
        battleCreationDTO.setMinStudentsInGroup(1);
        battleCreationDTO.setMaxStudentsInGroup(3);

        when(tournamentRepository.findById(tournamentId)).thenReturn(java.util.Optional.of(tournament));
        when(battleRepository.save(any(Battle.class))).thenReturn(mockBattleEntity);
        when(gitHubAPI.createRepository(any(String.class), any(String.class), any(boolean.class))).thenReturn(tournament.getName() + "-" + mockBattle.getName());


        BattleDTO created = this.battleService.createBattle(tournamentId, battleCreationDTO, mockZip, mockZip);

        //check if the attributes are the same
        assertEquals(mockBattle.getName(), created.getName());
        assertEquals(mockBattle.getRepositoryLink(), created.getRepositoryLink());
        assertEquals(mockBattle.getState(), created.getState());
        assertEquals(mockBattle.getMinStudentsInGroup(), created.getMinStudentsInGroup());
        assertEquals(mockBattle.getMaxStudentsInGroup(), created.getMaxStudentsInGroup());
        assertEquals(mockBattle.getTestRepositoryLink(), created.getTestRepositoryLink());
        assertEquals(mockBattle.getProgrammingLanguage(), created.getProgrammingLanguage());
        assertEquals(mockBattle.isManualScoringRequired(), created.isManualScoringRequired());
        assertEquals(mockBattle.getSubmissionDeadline(), created.getSubmissionDeadline());
        assertEquals(mockBattle.getSubscriptionDeadline(), created.getSubscriptionDeadline());

        // Verify with bad zip fails

        MockMultipartFile badMock = getBadZip1();

        assertThrows(InvalidBattleCreationException.class, () -> {
            this.battleService.createBattle(tournamentId, battleCreationDTO, badMock, badMock);
        });

        MockMultipartFile badMock2 = getBadZip2();

        assertThrows(InvalidBattleCreationException.class, () -> {
            this.battleService.createBattle(tournamentId, battleCreationDTO, badMock2, badMock2);
        });

    }

    private static  byte[] readFileToByteArray(File file) throws IOException{
        return Files.readAllBytes(file.toPath());
    }

    private File createGoodTempZipFile() throws IOException {

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

    private File createBadTempZipFile1() throws IOException {

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

    private File createBadTempZipFile2() throws IOException {

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

            // Adding src directory
            ZipEntry srcDirEntry = new ZipEntry("srac/");
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

    private MockMultipartFile getGoodZip() throws IOException{

        MockMultipartFile mockMultipartFile;

        try {
            File zipFile = createGoodTempZipFile();
            byte[] zipContent = readFileToByteArray(zipFile);

            mockMultipartFile = new MockMultipartFile(
                    "file", // Parameter name for the multipart file
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

    private MockMultipartFile getBadZip1() throws IOException{
        //Mock zip files
        MockMultipartFile mockMultipartFile;

        try {
            File zipFile = createBadTempZipFile1();
            byte[] zipContent = readFileToByteArray(zipFile);

            mockMultipartFile = new MockMultipartFile(
                    "file", // Parameter name for the multipart file
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

    private MockMultipartFile getBadZip2() throws IOException{
        //Mock zip files
        MockMultipartFile mockMultipartFile;

        try {
            File zipFile = createBadTempZipFile2();
            byte[] zipContent = readFileToByteArray(zipFile);

            mockMultipartFile = new MockMultipartFile(
                    "file", // Parameter name for the multipart file
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
}