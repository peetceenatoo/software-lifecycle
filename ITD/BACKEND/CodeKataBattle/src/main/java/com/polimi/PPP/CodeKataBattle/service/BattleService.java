package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BattleService {

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private BattleScoreRepository battleScoreRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BattleSubscriptionRepository battleSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BattleInviteRepository battleInviteRepository;

    public List<BattleDTO> getBattlesByTournamentId(Long tournamentId) {
        return battleRepository.findByTournamentId(tournamentId).stream()
                               .map(battle -> modelMapper.map(battle, BattleDTO.class))
                               .collect(Collectors.toList());
    }

    public List<BattleDTO> getEnrolledBattlesByTournamentId(Long tournamentId, Long userId) {
        return battleRepository.findBattlesByTournamentIdAndUserId(tournamentId, userId).stream()
                .map(battle -> modelMapper.map(battle, BattleDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<String> correctScore(Long submissionId, int correction) {
        Optional<BattleScore> battleScoreOpt = battleScoreRepository.findBySubmissionId(submissionId);

        if (battleScoreOpt.isPresent()) {
            BattleScore battleScore = battleScoreOpt.get();
            battleScore.setManualCorrection(correction);
            battleScoreRepository.save(battleScore);
            return Optional.of("Success");
        } else {
            throw new InvalidArgumentException("Invalid submission id");
        }
    }

    public Optional<BattleDTO> getBattleByIdEducator(Long battleId, Long userId) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isPresent()) {
            Battle battle = battleOpt.get();
            if (battle.getTournament().getUsers().stream().anyMatch(user -> user.getId().equals(userId))) {
                return Optional.of(modelMapper.map(battle, BattleDTO.class));
            }
        }
        return Optional.empty();
    }

    public Optional<BattleStudentDTO> getBattleByIdStudent(Long battleId, Long userId) {
        Optional<BattleSubscription> battleSubscriptionOpt = battleSubscriptionRepository.getBattleSubscriptionByBattleIdAndUserId(battleId, userId);
        if (battleSubscriptionOpt.isPresent()) {
            BattleSubscription battleSubscription = battleSubscriptionOpt.get();
            BattleStudentDTO battleStudentDTO = new BattleStudentDTO();
            battleStudentDTO.setBattle(modelMapper.map(battleSubscription.getBattle(), BattleDTO.class));
            battleStudentDTO.setGroupId(battleSubscription.getGroupId());
            battleStudentDTO.setUserId(battleSubscription.getUser().getId());
            return Optional.of(battleStudentDTO);
        }

        return Optional.empty();
    }


    public void closeBattle(Long battleId) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isPresent()) {
            Battle battle = battleOpt.get();
            battle.setState(BattleStateEnum.ENDED);
            battleRepository.save(battle);
        }
    }

    @Transactional
    public List<BattleRankingDTO> getBattleRanking(Long battleId) {
        List<BattleRankingGroupDTO> group_ranking = battleScoreRepository.calculateStudentRankingForBattle(battleId);
        List<BattleRankingDTO> ranking = new ArrayList<>();
        for (BattleRankingGroupDTO x: group_ranking) {
            List<String> usernames = battleSubscriptionRepository.findUsernamesByBattleId(battleId, x.getGroupId());
            BattleRankingDTO battleRankingDTO = new BattleRankingDTO();
            battleRankingDTO.setGroupId(x.getGroupId());
            battleRankingDTO.setUsernames(usernames);
            battleRankingDTO.setHighestScore(x.getHighestScore());
            ranking.add(battleRankingDTO);
        }
        return ranking;
    }


    @Transactional
    public void inviteUserToBattle(BattleEnrollDTO battleEnrollDTO) {
        Battle battle = battleRepository.findById(battleEnrollDTO.getBattleId()).orElseThrow(() -> new InvalidArgumentException("Battle not found"));
        User user = userRepository.findById(battleEnrollDTO.getUserId()).orElseThrow(() -> new InvalidArgumentException("User not found"));
        if (battleEnrollDTO.getUsernames().isEmpty() ) {
            throw new InvalidArgumentException("No users to invite");
        }
        for (String username : battleEnrollDTO.getUsernames()) {
            User invitedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Invited user not found"));
            BattleInvite invite = new BattleInvite();
            invite.setBattle(battle);
            invite.setUser(user);
            invite.setInvitedUser(invitedUser);
            invite.setState(BattleInviteStateEnum.PENDING); // Assuming an enum for the invite state
            battleInviteRepository.save(invite);
        }
    }

    @Transactional
    public void enrollBattle(BattleEnrollDTO battleEnrollDTO) {
        Battle battle = battleRepository.findById(battleEnrollDTO.getBattleId()).orElseThrow(() -> new InvalidArgumentException("Battle not found"));
        User user = userRepository.findById(battleEnrollDTO.getUserId()).orElseThrow(() -> new InvalidArgumentException("User not found"));
        BattleInvite invite = new BattleInvite();
        invite.setBattle(battle);
        invite.setUser(user);
        invite.setInvitedUser(null);
        invite.setState(BattleInviteStateEnum.ACCEPTED); // Assuming an enum for the invite state
        battleInviteRepository.save(invite);
    }

    //I can remove some checks to improve it
    public void enrollAndInviteBattle(BattleEnrollDTO battleEnrollDTO) {
        if (battleEnrollDTO.getUsernames().isEmpty()) {
            throw new InvalidArgumentException("No users to invite");
        }
        Battle battle = battleRepository.findById(battleEnrollDTO.getBattleId()).orElseThrow(() -> new InvalidArgumentException("Battle not found"));
        User user = userRepository.findById(battleEnrollDTO.getUserId()).orElseThrow(() -> new InvalidArgumentException("User not found"));
        if(battleEnrollDTO.getUsernames().size() > battle.getMaxStudentsInGroup()) {
            throw new InvalidArgumentException("Too many users to invite");
        }
        if(battleEnrollDTO.getUsernames().size() > battle.getMinStudentsInGroup()) {
            throw new InvalidArgumentException("Too few users to invite");
        }
        enrollBattle(battleEnrollDTO);
        inviteUserToBattle(battleEnrollDTO);
    }

    @Transactional
    public void acceptBattleInvite(Long inviteId) {

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
            }
        }else{
            if (invites.size() > battle.getMinStudentsInGroup() && invites.size() <= battle.getMaxStudentsInGroup()) {
                BattleSubscription subscription = new BattleSubscription();
                subscription.setBattle(battle);
                subscription.setUser(user);
                //find the group id of the other users that accepted that battle
                long groupId = battleSubscriptionRepository.findGroupIdByBattleIdAndUserId(battleId, userId);
                subscription.setGroupId(groupId);
            }
            else if (invites.size() < battle.getMinStudentsInGroup()) return;

            throw new InvalidArgumentException("Too many users in the group");
        }

    }


    public BattleDTO getBattleById(Long battleId) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isPresent()) {
            return modelMapper.map(battleOpt.get(), BattleDTO.class);
        } else {
            throw new InvalidArgumentException("Battle not found");
        }
    }
}
