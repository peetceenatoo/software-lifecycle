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
    List<Tournament> findByUsers_Id(Long userId); // For enrolled tournaments

    //Tournament findById(Long tournamentId);

    @Query("UPDATE Tournament t SET t.state = :newState WHERE t.id = :tournamentId")
    @Modifying
    @Transactional
    void updateStateForTournament(@Param("tournamentId") Long tournamentId, @Param("newState") TournamentStateEnum newState);

    @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Tournament t " +
            "JOIN t.users u " +
            "WHERE u.id = :userId AND t.id = :tournamentId")
    Boolean hasUserRightsOnTournament(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);

    @Query(value = "SELECT new com.polimi.PPP.CodeKataBattle.DTOs.TournamentRankingDTO(u.username, SUM(v.bestScore)) " +
            "FROM BestBattleScores v " +
            "JOIN Users u ON v.user_id = u.id " +
            "WHERE v.tournament_id = :tournamentId " +
            "GROUP BY u.username " +
            "ORDER BY SUM(v.bestScore) DESC", nativeQuery = true)
    List<TournamentRankingDTO> calculateStudentRankingForTournament(@Param("tournamentId") Long tournamentId);


}
