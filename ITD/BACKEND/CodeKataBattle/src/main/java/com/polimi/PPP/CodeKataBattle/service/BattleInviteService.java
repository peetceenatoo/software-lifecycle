package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleEnrollDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.BattleInviteDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.BattleSubscriptionDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.MessageDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleInviteRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleSubscriptionRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BattleInviteService {

    private final BattleInviteRepository battleInviteRepository;

    private final BattleRepository battleRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final BattleSubscriptionRepository battleSubscriptionRepository;

    private final JwtHelper jwtHelper;

    @Qualifier("emailProvider")
    private final NotificationProvider notificationProvider;

    @Autowired
    public BattleInviteService(BattleInviteRepository battleInviteRepository, BattleRepository battleRepository, UserRepository userRepository, ModelMapper modelMapper, BattleSubscriptionRepository battleSubscriptionRepository, JwtHelper jwtHelper,
                                NotificationProvider notificationProvider) {
        this.battleInviteRepository = battleInviteRepository;
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.battleSubscriptionRepository = battleSubscriptionRepository;
        this.jwtHelper = jwtHelper;
        this.notificationProvider = notificationProvider;
    }
    public void changeBattleInvitesState(Long battleInviteId, BattleInviteStateEnum oldState, BattleInviteStateEnum newState){

        if (oldState == null || newState == null) {
            throw new IllegalArgumentException("Old and new state must be not null");
        }

        if(oldState.equals(newState)){
            throw new IllegalArgumentException("Old and new state must be different");
        }

        if(oldState.equals(BattleInviteStateEnum.REJECTED)){
            throw new IllegalArgumentException("Old state cannot be REJECTED");
        }

        // Batch update using custom query
        this.battleInviteRepository.updateStateForBattle(battleInviteId, oldState, newState);
    }

    private boolean isUserEnrolledInBattle(Long battleId, Long userId) {
        return battleInviteRepository.findByBattleIdAndUserId(battleId, userId).isPresent();
    }

    private boolean hasUserAcceptedInviteForBattle(Long battleId, Long userId) {
        return battleInviteRepository.findByBattleIdAndUserIdAndState(battleId, userId, BattleInviteStateEnum.ACCEPTED).isPresent();
    }

    //I can remove some checks to improve it
    @Transactional
    public void enrollAndInviteBattle(BattleEnrollDTO battleEnrollDTO) {

        Battle battle = battleRepository.findById(battleEnrollDTO.getBattleId()).orElseThrow(() -> new InvalidArgumentException("Battle not found"));
        User user = userRepository.findById(battleEnrollDTO.getUserId()).orElseThrow(() -> new InvalidArgumentException("User not found"));
        if(battleEnrollDTO.getUsernames().size() + 1 > battle.getMaxStudentsInGroup()) {
            throw new InvalidArgumentException("Too many users to invite");
        }
        if(battleEnrollDTO.getUsernames().size() + 1  < battle.getMinStudentsInGroup()) {
            throw new InvalidArgumentException("Too few users to invite");
        }

        // Check invites
        for (String username : battleEnrollDTO.getUsernames()) {
            User invitedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidArgumentException("Invited user not found"));
            if (isUserEnrolledInBattle(battleEnrollDTO.getBattleId(), invitedUser.getId())
                    || hasUserAcceptedInviteForBattle(battleEnrollDTO.getBattleId(), invitedUser.getId())){
                throw new InvalidArgumentException("User already enrolled");
            }
        }

        enrollBattle(battleEnrollDTO);
        inviteUserToBattle(battleEnrollDTO);
    }

    @Transactional
    public List<BattleInviteDTO> inviteUserToBattle(BattleEnrollDTO battleEnrollDTO) {
        List <BattleInviteDTO> invites = new ArrayList<>();
        Optional<Battle> battle = battleRepository.findById(battleEnrollDTO.getBattleId());
        if (battle.isEmpty()) {
            throw new InvalidArgumentException("Battle not found");
        }
        Optional<BattleInvite> battleInvite = battleInviteRepository.findByBattleIdAndUserIdAndState(battleEnrollDTO.getBattleId(), battleEnrollDTO.getUserId(), BattleInviteStateEnum.ACCEPTED);

        if (battleInvite.isEmpty()) {
            throw new InvalidArgumentException("User not enrolled in the battle");
        }

        if(battle.get().getState() != BattleStateEnum.SUBSCRIPTION){
            throw new InvalidArgumentException("Subscription deadline expired");
        }

        User user = battleInvite.get().getUser();
        List<String> emails = new ArrayList<>();
        List<MessageDTO> messageDTOS = new ArrayList<>();

        for (String username : battleEnrollDTO.getUsernames()) {
            User invitedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidArgumentException("Invited user not found"));

            BattleInvite invite = new BattleInvite();
            invite.setBattle(battle.get());
            invite.setUser(user);
            invite.setInvitedUser(invitedUser);
            invite.setState(BattleInviteStateEnum.PENDING); // Assuming an enum for the invite state
            BattleInvite createdInvite = battleInviteRepository.save(invite);
            BattleInviteDTO inviteDTO = new BattleInviteDTO();
            inviteDTO.setBattleId(invite.getBattle().getId());
            inviteDTO.setUserId(invite.getUser().getId());
            inviteDTO.setInvitedUserid(invite.getInvitedUser().getId());
            inviteDTO.setState(invite.getState());
            invites.add(inviteDTO);

            String inviteToken = jwtHelper.generateInviteToken(invite.getId(), invite.getBattle().getSubscriptionDeadline());

            String link = "<a href='http://localhost:8080/api/battles/acceptInvitation/" + inviteToken + "'>Join the gorup!</a>";

            MessageDTO messageDTO = new MessageDTO(link,"You have been invited to take part in a battle!");
            emails.add(invitedUser.getEmail());
            messageDTOS.add(messageDTO);
;
        }


        // Callback after the transaction is committed
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {

            for (int i = 0;i<emails.size();i++){
                notificationProvider.sendNotification(messageDTOS.get(i), emails.get(i));
            }

            }

            // Implement other methods as needed or leave them as default
        });




        return invites;
    }

    @Transactional
    public List<BattleSubscriptionDTO> acceptBattleInvite(Long inviteId) {
        List<BattleSubscriptionDTO> subscriptions = new ArrayList<>();
        BattleInvite invite = battleInviteRepository.findById(inviteId)
                .orElseThrow(() -> new InvalidArgumentException("Invite not found"));
        Battle battle = invite.getBattle();
        Long battleId = battle.getId();
        User user = invite.getUser();
        Long userId = user.getId();

        if(battle.getState() != BattleStateEnum.SUBSCRIPTION){
            throw new InvalidArgumentException("Subscription deadline expired");
        }


        invite.setState(BattleInviteStateEnum.ACCEPTED);
        battleInviteRepository.save(invite);

        // Count the accepted invites
        //Long acceptedInvitesCount = battleInviteRepository.countByBattleIdAndState(battleId, BattleInviteStateEnum.ACCEPTED, userId);
        List<BattleInvite> invites = battleInviteRepository.getInvitesByBattleIdAndStateAndUserId(battleId, BattleInviteStateEnum.ACCEPTED, userId);

        // Check if the count meets the minimum group size constraint
        if (invites.size() == battle.getMinStudentsInGroup()) {
            // Create a new BattleSubscription
            long groupId = battleSubscriptionRepository.findMaxGroupIdInBattle(battleId) + 1;
            for (BattleInvite x: invites) {
                BattleSubscription subscription = new BattleSubscription();
                subscription.setBattle(battle);
                subscription.setUser(x.getUser());
                //generate a group id getting the max group id and adding 1
                subscription.setGroupId(groupId);
                battleSubscriptionRepository.save(subscription);
                BattleSubscriptionDTO subscriptionDTO = new BattleSubscriptionDTO();
                modelMapper.map(subscription, subscriptionDTO);
                subscriptions.add(subscriptionDTO);

            }
        }else{
            if (invites.size() > battle.getMinStudentsInGroup() && invites.size() <= battle.getMaxStudentsInGroup()) {
                BattleSubscription subscription = new BattleSubscription();
                subscription.setBattle(battle);
                subscription.setUser(invite.getInvitedUser());
                //find the group id of the other users that accepted that battle
                Optional<Long> groupId = battleSubscriptionRepository.findGroupIdByBattleIdAndUserId(battleId, userId);

                if(groupId.isEmpty()) throw new InvalidArgumentException("Group not found");

                subscription.setGroupId(groupId.get());
                battleSubscriptionRepository.save(subscription);
                BattleSubscriptionDTO subscriptionDTO = new BattleSubscriptionDTO();
                modelMapper.map(subscription, subscriptionDTO);
                subscriptions.add(subscriptionDTO);
            }
            else if (invites.size() < battle.getMinStudentsInGroup()) return new ArrayList<>();
            else  throw new InvalidArgumentException("Too many users in the group");
        }



        // Callback after the transaction is committed
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {

            MessageDTO messageDTO = new MessageDTO("Subscription to battle '" + battle.getName() + "' confirmed!","Enroll confirmed!");

            for (BattleSubscriptionDTO sub : subscriptions){
                Optional<User> userForEmail =  userRepository.findById(sub.getUserId());
                userForEmail.ifPresent(value -> notificationProvider.sendNotification(messageDTO, value.getEmail()));
            }

            }

            // Implement other methods as needed or leave them as default
        });



        return subscriptions;
    }

    @Transactional
    public BattleInviteDTO enrollBattle(BattleEnrollDTO battleEnrollDTO) {
        Battle battle = battleRepository.findById(battleEnrollDTO.getBattleId()).orElseThrow(() -> new InvalidArgumentException("Battle not found"));
        User user = userRepository.findById(battleEnrollDTO.getUserId()).orElseThrow(() -> new InvalidArgumentException("User not found"));

        Optional<BattleInvite> alreadyEnrolled= battleInviteRepository.findByBattleIdAndUserId(battleEnrollDTO.getBattleId(), battleEnrollDTO.getUserId());
        if(alreadyEnrolled.isPresent()){
            if (alreadyEnrolled.get().getInvitedUser() == null) {
                throw new InvalidArgumentException("User already enrolled");
            }
        }

        Optional<BattleSubscription> alreadySubscribed= battleSubscriptionRepository.getBattleSubscriptionByBattleIdAndUserId(battleEnrollDTO.getBattleId(), battleEnrollDTO.getUserId());
        if(alreadySubscribed.isPresent()){
            throw new InvalidArgumentException("User already subscribed");
        }

        BattleInvite invite = new BattleInvite();
        invite.setBattle(battle);
        invite.setUser(user);
        invite.setInvitedUser(null);
        invite.setState(BattleInviteStateEnum.ACCEPTED); // Assuming an enum for the invite state
        battleInviteRepository.save(invite);
        BattleInviteDTO inviteDTO = new BattleInviteDTO();
        modelMapper.map(invite, inviteDTO);

        // If the min of the battle is 1, the user is automatically enrolled
        if(battle.getMinStudentsInGroup() == 1){
            BattleSubscription subscription = new BattleSubscription();
            subscription.setBattle(battle);
            subscription.setUser(user);

            // Get the max group id and add 1
            Long groupId = battleSubscriptionRepository.findMaxGroupIdInBattle(battle.getId()) + 1;
            subscription.setGroupId(groupId);

            battleSubscriptionRepository.save(subscription);
        }

        return inviteDTO;
        //TODO: check if the user is already enrolled in the battle
    }

    @Transactional
    public void rejectGroupsNotReachedMinimum(Long battleId){
        Battle battle = battleRepository.findById(battleId).orElseThrow(() -> new InvalidArgumentException("Battle not found"));

        List<BattleInvite> invites = battleInviteRepository.findBattleInvitesByBattle_IdAndState(battleId, BattleInviteStateEnum.ACCEPTED);
        List<User> subscribed = battleSubscriptionRepository.findUsersByBattleId(battleId);

        Stream<User> stream = subscribed.stream();

        for(BattleInvite invite : invites){
            if(stream.noneMatch(x -> Objects.equals(x.getId(), invite.getUser().getId()))) {
                invite.setState(BattleInviteStateEnum.REJECTED);
                battleInviteRepository.save(invite);
            }
        }


    }
}
