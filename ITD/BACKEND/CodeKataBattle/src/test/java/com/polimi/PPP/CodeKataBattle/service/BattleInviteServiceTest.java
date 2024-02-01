package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BattleInviteServiceTest {

    private BattleInviteService battleInviteService;

    private UserDTO student;

    private TournamentDTO tournamentDTO;

    private MockMvc mockMvc;

    private UserDTO educator;

    @Mock
    private BattleRepository battleRepository;

    @Mock
    private BattleInviteRepository battleInviteRepository;

    private ModelMapper modelMapper;

    @Mock
    private BattleSubscriptionRepository battleSubscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationProvider notificationProvider;

    @Mock
    private JwtHelper jwtHelper;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        this.modelMapper = new ModelMapper();
        this.battleInviteService = new BattleInviteService(battleInviteRepository, battleRepository, userRepository, modelMapper, battleSubscriptionRepository, jwtHelper, notificationProvider);
    }

    @Test
    void testAcceptBattleInvite() {
        Long inviteId = 1L;
        Long battleId = 1L;
        Long userId = 1L;
        Long groupId = 1L;

        // Set up mock data
        User user = new User();
        user.setId(userId);

        Battle battle = new Battle();
        battle.setId(battleId);
        battle.setState(BattleStateEnum.SUBSCRIPTION);
        battle.setMinStudentsInGroup(1);
        battle.setMaxStudentsInGroup(3);

        BattleInvite invite = new BattleInvite();
        invite.setId(inviteId);
        invite.setBattle(battle);
        invite.setUser(user);
        invite.setState(BattleInviteStateEnum.PENDING);

        BattleSubscription subscription = new BattleSubscription();
        subscription.setBattle(battle);
        subscription.setUser(user);
        subscription.setGroupId(groupId);


        // Mock repository responses
        when(battleInviteRepository.findById(inviteId)).thenReturn(Optional.of(invite));
        when(battleRepository.findById(battleId)).thenReturn(Optional.of(battle));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(battleSubscriptionRepository.findGroupIdByBattleIdAndUserId(battleId, userId)).thenReturn(1L); // Assuming no existing subscription
        when(battleInviteRepository.countByBattleIdAndState(battleId, BattleInviteStateEnum.ACCEPTED, userId)).thenReturn(1); // Assuming this is the first acceptance

        // Mock repository saves
        when(battleInviteRepository.save(any(BattleInvite.class))).thenReturn(invite);
        when(battleSubscriptionRepository.save(any(BattleSubscription.class))).thenReturn(subscription);

        List<BattleInvite> battleInviteList = new ArrayList<>();
        battleInviteList.add(invite);
        when(battleInviteRepository.getInvitesByState(battleId, BattleInviteStateEnum.ACCEPTED, userId)).thenReturn(battleInviteList); // Assuming no existing accepted invite

        // Mock repository updates
        when(battleInviteRepository.updateStateForBattle(battleId, BattleInviteStateEnum.PENDING, BattleInviteStateEnum.ACCEPTED)).thenReturn(1);

        List<BattleSubscriptionDTO> subscriptions = battleInviteService.acceptBattleInvite(inviteId);

        assertEquals(1, subscriptions.size());
        assertEquals(groupId, subscriptions.get(0).getGroupId());
        assertEquals(battleId, subscriptions.get(0).getBattleId());
        assertEquals(userId, subscriptions.get(0).getUserId());

    }

    @Test
    void testEnrollBattle() {
        Long battleId = 1L;
        Long userId = 1L;
        Long groupId = 1L;

        // Set up mock data
        User user = new User();
        user.setId(userId);


        Battle battle = new Battle();
        battle.setId(battleId);
        battle.setState(BattleStateEnum.SUBSCRIPTION);
        battle.setMinStudentsInGroup(1);
        battle.setMaxStudentsInGroup(3);

        BattleInviteDTO battleInviteDTO = new BattleInviteDTO();
        battleInviteDTO.setBattleId(battleId);
        battleInviteDTO.setUserId(userId);
        battleInviteDTO.setState(BattleInviteStateEnum.ACCEPTED);

        BattleInvite invite = new BattleInvite();
        modelMapper.map(battleInviteDTO, invite);

        BattleEnrollDTO battleEnrollDTO = new BattleEnrollDTO();
        battleEnrollDTO.setBattleId(battleId);
        battleEnrollDTO.setUserId(userId);

        when(battleRepository.findById(battleId)).thenReturn(Optional.of(battle));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(battleInviteRepository.save(invite)).thenReturn(invite); // Assuming this is the first acceptance
        when(battleInviteRepository.findByBattleIdAndUserId(battleId, userId)).thenReturn(Optional.empty()); // Assuming no existing subscription
        //when(battleInviteRepository.countByBattleIdAndState(battleId, BattleInviteStateEnum.ACCEPTED, userId)).thenReturn(1); // Assuming this is the first acceptance
        when(battleSubscriptionRepository.getBattleSubscriptionByBattleIdAndUserId(battleId, userId)).thenReturn(Optional.empty()); // Assuming no existing subscription

        BattleInviteDTO result = battleInviteService.enrollBattle(battleEnrollDTO);

        assertEquals(result.getBattleId(), battleId);
        assertEquals(result.getUserId(), userId);
        assertEquals(result.getState(), BattleInviteStateEnum.ACCEPTED);
    }

    @Test
    void testInviteUserToBattle() {
        Long battleId = 1L;
        Long userId = 1L;
        Long groupId = 1L;

        // Set up mock data
        User user = new User();
        user.setId(userId);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);

        Battle battle = new Battle();
        battle.setId(battleId);
        battle.setState(BattleStateEnum.SUBSCRIPTION);
        battle.setMinStudentsInGroup(1);
        battle.setMaxStudentsInGroup(3);

        BattleEnrollDTO battleEnrollDTO = new BattleEnrollDTO();
        battleEnrollDTO.setBattleId(battleId);
        battleEnrollDTO.setUserId(userId);
        List<String> usernames = new ArrayList<>();
        usernames.add("user2");
        usernames.add("user3");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("user3");

        battleEnrollDTO.setUsernames(usernames);


        BattleInvite battleInvite = new BattleInvite();
        battleInvite.setBattle(battle);
        battleInvite.setUser(user);
        battleInvite.setState(BattleInviteStateEnum.ACCEPTED);

        when(battleRepository.findById(battleId)).thenReturn(Optional.of(battle));
        when(battleInviteRepository.findByBattleIdAndUserIdAndState(battleId,userId,BattleInviteStateEnum.ACCEPTED)).thenReturn(Optional.of(battleInvite)); // Assuming no existing subscription
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));
        when(userRepository.findByUsername("user3")).thenReturn(Optional.of(user3));
        when(battleInviteRepository.save(any(BattleInvite.class))).thenReturn(battleInvite); // Assuming this is the first acceptance
        when(battleInviteRepository.countByBattleIdAndState(battleId, BattleInviteStateEnum.ACCEPTED, userId)).thenReturn(1); // Assuming this is the first acceptance
        when(battleSubscriptionRepository.getBattleSubscriptionByBattleIdAndUserId(battleId, userId)).thenReturn(Optional.empty()); // Assuming no existing subscription

        BattleInvite battleInvite2 = new BattleInvite();
        battleInvite2.setBattle(battle);
        battleInvite2.setUser(user2);
        battleInvite2.setInvitedUser(user);
        battleInvite2.setState(BattleInviteStateEnum.PENDING);

        BattleInvite battleInvite3 = new BattleInvite();
        battleInvite3.setBattle(battle);
        battleInvite3.setUser(user3);
        battleInvite3.setInvitedUser(user);
        battleInvite3.setState(BattleInviteStateEnum.PENDING);

        BattleInviteDTO battleInviteDTO2 = new BattleInviteDTO();
        modelMapper.map(battleInvite2, battleInviteDTO2);

        BattleInviteDTO battleInviteDTO3 = new BattleInviteDTO();
        modelMapper.map(battleInvite3, battleInviteDTO3);


        when(battleInviteRepository.save(any(BattleInvite.class))).thenReturn(battleInvite2); // Assuming this is the first acceptance
        when(battleInviteRepository.save(any(BattleInvite.class))).thenReturn(battleInvite3); // Assuming this is the first acceptance

        List<BattleInviteDTO> result = battleInviteService.inviteUserToBattle(battleEnrollDTO);

        assertEquals(result.get(0).getBattleId(), battleId);
        assertEquals(result.get(0).getUserId(), user.getId());
        assertEquals(result.get(0).getInvitedUserid(), user2.getId());
        assertEquals(result.get(0).getState(), BattleInviteStateEnum.PENDING);
        assertEquals(result.get(1).getBattleId(), battleId);
        assertEquals(result.get(1).getUserId(), user.getId());
        assertEquals(result.get(1).getInvitedUserid(), user3.getId());
        assertEquals(result.get(1).getState(), BattleInviteStateEnum.PENDING);
    }


}