package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.BattleInvite;
import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BattleInviteRepository extends JpaRepository<BattleInvite, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE BattleInvite bi SET bi.state = :newState WHERE bi.battle.id = :battleId AND bi.state = :currentState")
    int updateStateForBattle(Long battleId, BattleInviteStateEnum currentState, BattleInviteStateEnum newState);


}
