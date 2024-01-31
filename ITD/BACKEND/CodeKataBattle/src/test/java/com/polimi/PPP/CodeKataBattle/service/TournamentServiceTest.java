package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.TournamentCreationDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.TournamentDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.TournamentRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.*;

import static com.polimi.PPP.CodeKataBattle.Model.RoleEnum.ROLE_STUDENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class TournamentServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;

    private TournamentService tournamentService;

    @Mock
    private BattleRepository battleRepository;

    @Mock
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.tournamentService  = new TournamentService(tournamentRepository, battleRepository, userRepository, modelMapper, eventPublisher);
    }

    @Test
    public void testUserHasRights(){

        User user1 = new User();
        user1.setId(1L);
        Role studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(ROLE_STUDENT);
        user1.setRole(studentRole);
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setEmail("stud@stud.com");
        user1.setName("student");
        user1.setSurname("student surname");
        user1.setTournaments(new HashSet<>());

        User user2 = new User();
        user2.setId(2L);
        Role educatorRole = new Role();
        educatorRole.setId(2L);
        educatorRole.setName(RoleEnum.ROLE_EDUCATOR);
        user2.setRole(educatorRole);
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setEmail("edu@edu.com");
        user2.setName("educator");
        user2.setSurname("educator surname");
        user2.setTournaments(new HashSet<>());

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("tournament1");
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament.setDeadline(ZonedDateTime.now());
        tournament.setUsers(new HashSet<>());
        tournament.getUsers().add(user1);

        user1.getTournaments().add(tournament);

        when(tournamentRepository.hasUserRightsOnTournament(1L, 1L)).thenReturn(true);
        when(tournamentRepository.hasUserRightsOnTournament(2L, 1L)).thenReturn(false);
        when(tournamentRepository.hasUserRightsOnTournament(1L, 2L)).thenReturn(false);
        when(tournamentRepository.hasUserRightsOnTournament(2L, 2L)).thenReturn(false);

        assertTrue(tournamentService.hasUserRightsOnTournament(1L, 1L));
        assertFalse(tournamentService.hasUserRightsOnTournament(2L, 1L));
        assertFalse(tournamentService.hasUserRightsOnTournament(1L, 2L));
        assertFalse(tournamentService.hasUserRightsOnTournament(2L, 2L));

    }
    @Test
    public void testGetTournamentById(){

        User user1 = new User();
        user1.setId(1L);
        Role studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(ROLE_STUDENT);
        user1.setRole(studentRole);
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setEmail("stud@stud.com");
        user1.setName("student");
        user1.setSurname("student surname");
        user1.setTournaments(new HashSet<>());

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("tournament1");
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament.setDeadline(ZonedDateTime.now());
        tournament.setUsers(new HashSet<>());
        tournament.getUsers().add(user1);

        user1.getTournaments().add(tournament);

        TournamentDTO tournamentDTO = new TournamentDTO();
        modelMapper.map(tournament, tournamentDTO);

        when(tournamentRepository.findById(1L)).thenReturn(java.util.Optional.of(tournament));

        assertEquals(tournamentDTO, tournamentService.getTournamentById(1L));

        Tournament tournament2 = new Tournament();
        tournament2.setId(2L);
        tournament2.setName("tournament1");
        tournament2.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament2.setDeadline(ZonedDateTime.now());
        tournament2.setUsers(new HashSet<>());
        tournament2.getUsers().add(user1);
        when(tournamentRepository.findById(2L)).thenReturn(java.util.Optional.of(tournament2));

        assertNotEquals(tournamentDTO, tournamentService.getTournamentById(2L));


    }
    @Test
    public void testGetTournaments(){

        List<Tournament> tournaments = new ArrayList<>();

        Tournament tournamentSub = new Tournament();
        tournamentSub.setId(1L);
        tournamentSub.setName("tournament1");
        tournamentSub.setState(TournamentStateEnum.SUBSCRIPTION);
        tournamentSub.setDeadline(ZonedDateTime.now());
        tournamentSub.setUsers(new HashSet<>());
        tournaments.add(tournamentSub);

        Tournament tournamentOngoing = new Tournament();
        tournamentOngoing.setId(2L);
        tournamentOngoing.setName("tournament2");
        tournamentOngoing.setState(TournamentStateEnum.ONGOING);
        tournamentOngoing.setDeadline(ZonedDateTime.now());
        tournamentOngoing.setUsers(new HashSet<>());
        tournaments.add(tournamentOngoing);

        Tournament tournamentEnded = new Tournament();
        tournamentEnded.setId(3L);
        tournamentEnded.setName("tournament3");
        tournamentEnded.setState(TournamentStateEnum.ENDED);
        tournamentEnded.setDeadline(ZonedDateTime.now());
        tournamentEnded.setUsers(new HashSet<>());
        tournaments.add(tournamentEnded);


        List<Tournament> subs = new ArrayList<>();
        subs.add(tournamentSub);

        List<Tournament> ongoings = new ArrayList<>();
        ongoings.add(tournamentOngoing);

        List<Tournament> ended = new ArrayList<>();
        ended.add(tournamentEnded);

        when(tournamentRepository.findByState(TournamentStateEnum.SUBSCRIPTION)).thenReturn(subs);
        when(tournamentRepository.findByState(TournamentStateEnum.ONGOING)).thenReturn(ongoings);
        when(tournamentRepository.findByState(TournamentStateEnum.ENDED)).thenReturn(ended);
        when(tournamentRepository.findAll()).thenReturn(tournaments);

        List<TournamentDTO> allDTO = new ArrayList<>();
        List<TournamentDTO> subsDTO = new ArrayList<>();
        List<TournamentDTO> ongoingsDTO = new ArrayList<>();
        List<TournamentDTO> endedDTO = new ArrayList<>();

        for(Tournament t : tournaments){
            TournamentDTO tournamentDTO = new TournamentDTO();
            modelMapper.map(t, tournamentDTO);
            allDTO.add(tournamentDTO);
        }

        for(Tournament t : subs){
            TournamentDTO tournamentDTO = new TournamentDTO();
            modelMapper.map(t, tournamentDTO);
            subsDTO.add(tournamentDTO);
        }

        for(Tournament t : ongoings){
            TournamentDTO tournamentDTO = new TournamentDTO();
            modelMapper.map(t, tournamentDTO);
            ongoingsDTO.add(tournamentDTO);
        }

        for(Tournament t : ended){
            TournamentDTO tournamentDTO = new TournamentDTO();
            modelMapper.map(t, tournamentDTO);
            endedDTO.add(tournamentDTO);
        }

        assertTrue(tournamentService.getTournaments(null).containsAll(allDTO));
        assertEquals(tournamentService.getTournaments(null).size(), allDTO.size());

        assertTrue(tournamentService.getTournaments(TournamentStateEnum.SUBSCRIPTION).containsAll(subsDTO));
        assertEquals(tournamentService.getTournaments(TournamentStateEnum.SUBSCRIPTION).size(), subsDTO.size());

        assertTrue(tournamentService.getTournaments(TournamentStateEnum.ONGOING).containsAll(ongoingsDTO));
        assertEquals(tournamentService.getTournaments(TournamentStateEnum.ONGOING).size(), ongoingsDTO.size());

        assertTrue(tournamentService.getTournaments(TournamentStateEnum.ENDED).containsAll(endedDTO));
        assertEquals(tournamentService.getTournaments(TournamentStateEnum.ENDED).size(), endedDTO.size());


        when(tournamentRepository.findById(46464L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> tournamentService.getTournamentById(46464L));

    }
    @Test
    public void testGetManagedEnrolledTournaments() {

        User user2 = new User();
        user2.setId(2L);
        Role educatorRole = new Role();
        educatorRole.setId(2L);
        educatorRole.setName(RoleEnum.ROLE_EDUCATOR);
        user2.setRole(educatorRole);
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setEmail("edu@mail.com");
        user2.setName("educator");
        user2.setSurname("educator surname");
        user2.setTournaments(new HashSet<>());

        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        tournament1.setName("tournament1");
        tournament1.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament1.setDeadline(ZonedDateTime.now());
        tournament1.setUsers(new HashSet<>());
        tournament1.getUsers().add(user2);

        user2.getTournaments().add(tournament1);

        UserDTO educatorDTO = new UserDTO();
        modelMapper.map(user2, educatorDTO);

        TournamentDTO tournamentDTO = new TournamentDTO();
        modelMapper.map(tournament1, tournamentDTO);

        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(tournament1);

        List<TournamentDTO> tournamentsDTO = new ArrayList<>();
        tournamentsDTO.add(tournamentDTO);

        when(tournamentRepository.findByUsers_Id(2L)).thenReturn(tournaments);

        assertTrue(tournamentService.getManagedTournaments(2L).containsAll(tournamentsDTO));
        assertEquals(tournamentService.getManagedTournaments(2L).size(), tournamentsDTO.size());

        assertTrue(tournamentService.getEnrolledTournaments(2L).containsAll(tournamentsDTO));
        assertEquals(tournamentService.getEnrolledTournaments(2L).size(), tournamentsDTO.size());

    }
    @Test
    public void testChangeState(){
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("tournament1");
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament.setDeadline(ZonedDateTime.now());
        tournament.setUsers(new HashSet<>());

        Tournament tournamentUpdated = new Tournament();
        tournamentUpdated.setId(1L);
        tournamentUpdated.setName("tournament1");
        tournamentUpdated.setState(TournamentStateEnum.ONGOING);
        tournamentUpdated.setDeadline(ZonedDateTime.now());
        tournamentUpdated.setUsers(new HashSet<>());



        when(tournamentRepository.findById(1L)).thenReturn(java.util.Optional.of(tournament));
        when(tournamentRepository.save(tournament)).thenReturn(tournamentUpdated);

        assertThrows(IllegalStateException.class, () -> tournamentService.updateStateForTournament(1L, TournamentStateEnum.SUBSCRIPTION));
        assertDoesNotThrow(() -> tournamentService.updateStateForTournament(1L, TournamentStateEnum.ONGOING));

        tournamentUpdated.setState(TournamentStateEnum.ENDED);
        TournamentDTO tournamentDTO = new TournamentDTO();
        modelMapper.map(tournamentUpdated, tournamentDTO);

        assertEquals(tournamentDTO, tournamentService.updateStateForTournament(1L, TournamentStateEnum.ENDED));
    }
    @Test
    public void testCreateTournament(){

        User edu1 = new User();
        edu1.setId(1L);
        Role educatorRole = new Role();
        educatorRole.setId(1L);
        educatorRole.setName(RoleEnum.ROLE_EDUCATOR);
        edu1.setRole(educatorRole);
        edu1.setUsername("user1");
        edu1.setPassword("password1");
        edu1.setEmail("edu1@gmail.com");
        edu1.setName("educator1");
        edu1.setSurname("educator1 surname");
        edu1.setTournaments(new HashSet<>());

        User edu2 = new User();
        edu2.setId(2L);
        edu2.setRole(educatorRole);
        edu2.setUsername("user2");
        edu2.setPassword("password2");
        edu2.setEmail("edu2@gmail.com");
        edu2.setName("educator2");
        edu2.setSurname("educator2 surname");
        edu2.setTournaments(new HashSet<>());

        TournamentCreationDTO tournamentCreationDTO = new TournamentCreationDTO();
        tournamentCreationDTO.setTournamentName("tournament1");
        tournamentCreationDTO.setEducatorsInvited(new ArrayList<Long>());
        tournamentCreationDTO.getEducatorsInvited().add(1L);
        tournamentCreationDTO.getEducatorsInvited().add(2L);
        tournamentCreationDTO.setRegistrationDeadline(ZonedDateTime.now());

        Tournament created = new Tournament();
        created.setId(1L);
        created.setName("tournament1");
        created.setState(TournamentStateEnum.SUBSCRIPTION);
        created.setDeadline(tournamentCreationDTO.getRegistrationDeadline());
        created.setUsers(new HashSet<>());
        created.getUsers().add(edu1);
        created.getUsers().add(edu2);

        TournamentDTO resultDTO = new TournamentDTO();
        resultDTO.setId(1L);
        resultDTO.setName("tournament1");
        resultDTO.setState(TournamentStateEnum.SUBSCRIPTION);
        resultDTO.setDeadline(tournamentCreationDTO.getRegistrationDeadline());

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(created);
        when(userRepository.findById(1L)).thenReturn(Optional.of(edu1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(edu2));

        assertEquals(resultDTO, tournamentService.createTournament(tournamentCreationDTO));
    }
    @Test
    public void testCloseTournament() {

        // Empty tournament
        Tournament noBattlesTournament = new Tournament();
        noBattlesTournament.setId(1L);
        noBattlesTournament.setName("tournament1");
        noBattlesTournament.setState(TournamentStateEnum.ONGOING);
        noBattlesTournament.setDeadline(ZonedDateTime.now());
        noBattlesTournament.setBattles(new HashSet<>());

        // Ended tournament
        Tournament endedTournament = new Tournament();
        endedTournament.setId(2L);
        endedTournament.setName("tournament2");
        endedTournament.setState(TournamentStateEnum.ENDED);
        endedTournament.setDeadline(ZonedDateTime.now());
        endedTournament.setBattles(new HashSet<>());

        // Tournament with ongoing battles
        Tournament ongoingBattlesTournament = new Tournament();
        ongoingBattlesTournament.setId(3L);
        ongoingBattlesTournament.setName("tournament3");
        ongoingBattlesTournament.setState(TournamentStateEnum.ONGOING);
        ongoingBattlesTournament.setDeadline(ZonedDateTime.now());
        ongoingBattlesTournament.setBattles(new HashSet<>());

        Battle ongoingBattle = new Battle();
        ongoingBattle.setId(1L);
        ongoingBattle.setState(BattleStateEnum.ONGOING);
        ongoingBattle.setTournament(ongoingBattlesTournament);
        ongoingBattlesTournament.getBattles().add(ongoingBattle);


        // Tournament that can be closed
        Tournament tournament = new Tournament();
        tournament.setId(4L);
        tournament.setName("tournament4");
        tournament.setState(TournamentStateEnum.ONGOING);
        tournament.setDeadline(ZonedDateTime.now());
        tournament.setBattles(new HashSet<>());

        Battle endedBattle = new Battle();
        endedBattle.setId(2L);
        endedBattle.setState(BattleStateEnum.ENDED);
        endedBattle.setTournament(tournament);
        tournament.getBattles().add(endedBattle);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(noBattlesTournament));
        when(tournamentRepository.findById(2L)).thenReturn(Optional.of(endedTournament));
        when(tournamentRepository.findById(3L)).thenReturn(Optional.of(ongoingBattlesTournament));
        when(tournamentRepository.findById(4L)).thenReturn(Optional.of(tournament));

        when(tournamentRepository.hasUserRightsOnTournament(1L, 1L)).thenReturn(true);
        when(tournamentRepository.hasUserRightsOnTournament(1L, 2L)).thenReturn(true);
        when(tournamentRepository.hasUserRightsOnTournament(1L, 3L)).thenReturn(true);
        when(tournamentRepository.hasUserRightsOnTournament(1L, 4L)).thenReturn(true);

        when(tournamentRepository.hasUserRightsOnTournament(2L, 4L)).thenReturn(false);

        when(battleRepository.findByTournamentId(1L)).thenReturn(new ArrayList<>());
        when(battleRepository.findByTournamentId(2L)).thenReturn(new ArrayList<>());
        when(battleRepository.findByTournamentId(3L)).thenReturn(new ArrayList<>(Arrays.asList(ongoingBattle)));
        when(battleRepository.findByTournamentId(4L)).thenReturn(new ArrayList<>(Arrays.asList(endedBattle)));


        // Check
        TournamentDTO firstEnded = new TournamentDTO();
        modelMapper.map(noBattlesTournament, firstEnded);
        firstEnded.setState(TournamentStateEnum.ENDED);

        Tournament noBattlesEndedTournament = new Tournament();
        modelMapper.map(noBattlesTournament, noBattlesEndedTournament);
        noBattlesEndedTournament.setState(TournamentStateEnum.ENDED);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(noBattlesEndedTournament);

        TournamentDTO returned = tournamentService.closeTournament(1L, 1L);

        assertEquals(firstEnded, returned);

        // Already ended
        assertThrows(IllegalStateException.class, () -> tournamentService.closeTournament(2L, 1L));

        // Ongoing battles
        assertThrows(IllegalStateException.class, () -> tournamentService.closeTournament(3L,  1L));

        // Can be closed
        TournamentDTO secondEnded = new TournamentDTO();
        modelMapper.map(tournament, secondEnded);
        secondEnded.setState(TournamentStateEnum.ENDED);

        Tournament tournamentEnded = new Tournament();
        modelMapper.map(tournament, tournamentEnded);
        tournamentEnded.setState(TournamentStateEnum.ENDED);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournamentEnded);


        assertEquals(secondEnded, tournamentService.closeTournament(4L, 1L));

        assertThrows(IllegalArgumentException.class, () -> tournamentService.closeTournament(4L, 2L));
    }
    @Test
    public void testEnrollInTournament(){

        // A User
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setPassword("password1");
        user.setEmail("user@email.com");
        user.setName("user");
        user.setSurname("user surname");
        user.setTournaments(new HashSet<>());

        // A Tournament
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("tournament1");
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament.setDeadline(ZonedDateTime.now());
        tournament.setUsers(new HashSet<>());

        // An Ended Tournament
        Tournament endedTournament = new Tournament();
        endedTournament.setId(2L);
        endedTournament.setName("tournament2");
        endedTournament.setState(TournamentStateEnum.ENDED);
        endedTournament.setDeadline(ZonedDateTime.now());
        endedTournament.setUsers(new HashSet<>());


        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.findById(2L)).thenReturn(Optional.of(endedTournament));
        when(tournamentRepository.findById(3L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        // Check
        assertThrows(IllegalStateException.class, () -> tournamentService.enrollUserInTournament(2L, 1L));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.enrollUserInTournament(3L, 1L));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.enrollUserInTournament(1L, 3L));

        assertDoesNotThrow(() -> tournamentService.enrollUserInTournament(1L, 1L));
    }
    @Test
    public void testSearchTournamentsByKeyword(){
        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        tournament1.setName("tournament1");
        tournament1.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament1.setDeadline(ZonedDateTime.now());
        tournament1.setUsers(new HashSet<>());

        Tournament tournament2 = new Tournament();
        tournament2.setId(20L);
        tournament2.setName("tournament2");
        tournament2.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament2.setDeadline(ZonedDateTime.now());
        tournament2.setUsers(new HashSet<>());


        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(tournament1);
        tournaments.add(tournament2);

        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("tournament", -1L)).thenReturn(tournaments);
        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("tournament1", -1L)).thenReturn(new ArrayList<>(Arrays.asList(tournament1)));
        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("tournament2", -1L)).thenReturn(new ArrayList<>(Arrays.asList(tournament2)));
        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("tournament3", -1L)).thenReturn(new ArrayList<>());
        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("1", 1L)).thenReturn(new ArrayList<>(Arrays.asList(tournament1)));
        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("20", 20L)).thenReturn(new ArrayList<>(Arrays.asList(tournament2)));
        when(tournamentRepository.findByNameContainingIgnoreCaseOrIdIs("3", 3L)).thenReturn(new ArrayList<>());

        List<TournamentDTO> tournamentsDTO = new ArrayList<>();
        for(Tournament t : tournaments){
            TournamentDTO tournamentDTO = new TournamentDTO();
            modelMapper.map(t, tournamentDTO);
            tournamentsDTO.add(tournamentDTO);
        }

        List<TournamentDTO> tournament1DTO = new ArrayList<>();
        TournamentDTO tournamentDTO = new TournamentDTO();
        modelMapper.map(tournament1, tournamentDTO);
        tournament1DTO.add(tournamentDTO);

        List<TournamentDTO> tournament2DTO = new ArrayList<>();
        TournamentDTO tournamentDTO2 = new TournamentDTO();
        modelMapper.map(tournament2, tournamentDTO2);
        tournament2DTO.add(tournamentDTO2);

        assertTrue(tournamentService.searchTournamentsByKeyword("tournament").containsAll(tournamentsDTO));
        assertEquals(tournamentService.searchTournamentsByKeyword("tournament").size(), tournamentsDTO.size());

        assertTrue(tournamentService.searchTournamentsByKeyword("tournament1").containsAll(tournament1DTO));
        assertEquals(tournamentService.searchTournamentsByKeyword("tournament1").size(), tournament1DTO.size());

        assertTrue(tournamentService.searchTournamentsByKeyword("tournament2").containsAll(tournament2DTO));
        assertEquals(tournamentService.searchTournamentsByKeyword("tournament2").size(), tournament2DTO.size());

        assertTrue(tournamentService.searchTournamentsByKeyword("tournament3").isEmpty());

        assertTrue(tournamentService.searchTournamentsByKeyword("1").containsAll(tournament1DTO));
        assertEquals(tournamentService.searchTournamentsByKeyword("1").size(), tournament1DTO.size());

        assertTrue(tournamentService.searchTournamentsByKeyword("20").containsAll(tournament2DTO));
        assertEquals(tournamentService.searchTournamentsByKeyword("20").size(), tournament2DTO.size());

        assertTrue(tournamentService.searchTournamentsByKeyword("3").isEmpty());


    }
}
