package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.DTOs.BestBattleScoreDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.TournamentRankingDTO;
import com.polimi.PPP.CodeKataBattle.Model.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByState(TournamentStateEnum state);
    List<Tournament> findByNameContainingIgnoreCaseOrIdIs(String name, Long id);

    @Query(value = "SELECT t FROM Tournament t WHERE (t.name LIKE %:name% OR t.id = :id) AND t.state = :state")
    List<Tournament> findByNameContainingIgnoreCaseOrIdIsAndState(String name, Long id, TournamentStateEnum state);

    List<Tournament> findByUsers_Id(Long userId); // For enrolled tournaments

    @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Tournament t " +
            "JOIN t.users u " +
            "WHERE u.id = :userId AND t.id = :tournamentId")
    Boolean hasUserRightsOnTournament(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);


    @Query(value = "SELECT u.username as username, SUM(bbs.bestScore) as score " +
            "FROM BestBattleScores bbs " +
            "    JOIN battles b ON bbs.battle_fk = b.id " +
            "    JOIN battle_subscriptions bs on (bs.battle_id = bbs.battle_fk and bbs.group_id = bs.group_id) " +
            "    JOIN users u on (u.id = bs.user_id) " +
            "WHERE tournament_fk=:tournamentId " +
            "GROUP BY u.username" , nativeQuery = true)
    List<Object[]> calculateStudentRankingForTournament(@Param("tournamentId") Long tournamentId);


}
