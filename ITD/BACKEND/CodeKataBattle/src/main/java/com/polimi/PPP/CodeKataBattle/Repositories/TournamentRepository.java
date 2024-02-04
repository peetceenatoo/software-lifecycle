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

    @Query(value = "SELECT u.username, SUM(v.bestScore) as totalScore " +
            "FROM BestBattleScores v " +
            "JOIN users u ON v.user_id = u.id " +
            "JOIN battles b ON v.battle_id = b.id " +
            "WHERE b.tournament_fk = :tournamentId " +
            "GROUP BY u.username " +
            "ORDER BY totalScore DESC", nativeQuery = true)
    List<Object[]> calculateStudentRankingForTournament(@Param("tournamentId") Long tournamentId);


}
