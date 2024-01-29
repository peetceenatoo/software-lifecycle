package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.BattleInvite;
import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleInviteRepository extends JpaRepository<BattleInvite, Long> {


    @Query("SELECT COUNT(bi) FROM BattleInvite bi WHERE bi.battle.id = :battleId AND bi.state = :state AND bi.InvitedUser.id = :userId")
    Long countByBattleIdAndState(@Param("battleId") Long battleId, @Param("state") BattleInviteStateEnum state, @Param("userId") Long userId);

    @Query("SELECT bi FROM BattleInvite bi WHERE bi.battle.id = :battleId AND bi.state = :battleInviteStateEnum AND bi.InvitedUser.id = :userId")
    List<BattleInvite> getAcceptedInvite(Long battleId, BattleInviteStateEnum battleInviteStateEnum, Long userId);

}
