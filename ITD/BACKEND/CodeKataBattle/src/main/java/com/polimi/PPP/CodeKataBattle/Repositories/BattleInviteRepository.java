package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.BattleInvite;
import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleInviteRepository extends JpaRepository<BattleInvite, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE BattleInvite bi SET bi.state = :newState WHERE bi.battle.id = :battleId AND bi.state = :currentState")
    int updateStateForBattle(Long battleId, BattleInviteStateEnum currentState, BattleInviteStateEnum newState);


    @Query("SELECT COUNT(bi) FROM BattleInvite bi WHERE bi.battle.id = :battleId AND bi.state = :state AND bi.InvitedUser.id = :userId")
    int countByBattleIdAndState(@Param("battleId") Long battleId, @Param("state") BattleInviteStateEnum state, @Param("userId") Long userId);

    @Query("SELECT bi FROM BattleInvite bi WHERE bi.battle.id = :battleId AND bi.state = :battleInviteStateEnum AND bi.InvitedUser.id = :userId")
    List<BattleInvite> getInvitesByState(Long battleId, BattleInviteStateEnum battleInviteStateEnum, Long userId);

    List<BattleInvite> findBattleInvitesByBattle_IdAndState(Long battleId, BattleInviteStateEnum state);

    Optional<BattleInvite> findByBattleIdAndUserId(Long battleId, Long userId);

    Optional<BattleInvite> findByBattleIdAndUserIdAndState(Long battleId, Long userId, BattleInviteStateEnum state);

}
