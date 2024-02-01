package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleEnrollDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.BattleInviteDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.BattleSubscriptionDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleInviteRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleSubscriptionRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BattleInviteService {

    private final BattleInviteRepository battleInviteRepository;

    private final BattleRepository battleRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final BattleSubscriptionRepository battleSubscriptionRepository;



    @Autowired
    public BattleInviteService(BattleInviteRepository battleInviteRepository, BattleRepository battleRepository, UserRepository userRepository, ModelMapper modelMapper, BattleSubscriptionRepository battleSubscriptionRepository) {
        this.battleInviteRepository = battleInviteRepository;
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.battleSubscriptionRepository = battleSubscriptionRepository;
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

    //I can remove some checks to improve it
    @Transactional
    public void enrollAndInviteBattle(BattleEnrollDTO battleEnrollDTO) {
        if (battleEnrollDTO.getUsernames().isEmpty()) {
            throw new InvalidArgumentException("No users to invite");
        }
        Battle battle = battleRepository.findById(battleEnrollDTO.getBattleId()).orElseThrow(() -> new InvalidArgumentException("Battle not found"));
        User user = userRepository.findById(battleEnrollDTO.getUserId()).orElseThrow(() -> new InvalidArgumentException("User not found"));
        if(battleEnrollDTO.getUsernames().size() + 1 > battle.getMaxStudentsInGroup()) {
            throw new InvalidArgumentException("Too many users to invite");
        }
        if(battleEnrollDTO.getUsernames().size() + 1  < battle.getMinStudentsInGroup()) {
            throw new InvalidArgumentException("Too few users to invite");
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
        User user = battleInvite.get().getUser();

        for (String username : battleEnrollDTO.getUsernames()) {
            User invitedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidArgumentException("Invited user not found"));

            BattleInvite invite = new BattleInvite();
            invite.setBattle(battle.get());
            invite.setUser(user);
            invite.setInvitedUser(invitedUser);
            invite.setState(BattleInviteStateEnum.PENDING); // Assuming an enum for the invite state
            battleInviteRepository.save(invite);
            BattleInviteDTO inviteDTO = new BattleInviteDTO();
            inviteDTO.setBattleId(invite.getBattle().getId());
            inviteDTO.setUserId(invite.getUser().getId());
            inviteDTO.setInvitedUserid(invite.getInvitedUser().getId());
            inviteDTO.setState(invite.getState());
            invites.add(inviteDTO);
        }
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

        List<BattleInvite> invites = battleInviteRepository.getAcceptedInvite(battleId, BattleInviteStateEnum.ACCEPTED, userId);

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
                subscription.setUser(user);
                //find the group id of the other users that accepted that battle
                long groupId = battleSubscriptionRepository.findGroupIdByBattleIdAndUserId(battleId, userId);
                subscription.setGroupId(groupId);
                battleSubscriptionRepository.save(subscription);
                BattleSubscriptionDTO subscriptionDTO = new BattleSubscriptionDTO();
                modelMapper.map(subscription, subscriptionDTO);
                subscriptions.add(subscriptionDTO);
            }
            else if (invites.size() < battle.getMinStudentsInGroup()) return new ArrayList<>();
            else  throw new InvalidArgumentException("Too many users in the group");
        }
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
        //this doesn't have id
        return inviteDTO;
        //TODO: check if the user is already enrolled in the battle
    }
}
