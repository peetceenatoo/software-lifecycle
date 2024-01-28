package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.TournamentDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.service.BattleInviteService;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
@Slf4j
public class DeadlineScheduler {
    private final TaskScheduler taskScheduler;
    private final BattleService battleService; // Assuming this service handles the logic
    private final BattleInviteService battleInviteService;
    private final TournamentService tournamentService;

    @Autowired
    public DeadlineScheduler(TaskScheduler taskScheduler, BattleService battleService, TournamentService tournamentService, BattleInviteService battleInviteService) {
        this.battleService = battleService;
        this.tournamentService = tournamentService;
        this.taskScheduler = taskScheduler;
        this.battleInviteService = battleInviteService;
    }

    @PostConstruct
    public void init(){

        //TODO: we should correctly handle changes of deadlines and reschedule the tasks
        // .
        // Immediate processing of past deadlines after a crash/restart happens automatically thanks to
        // how Spirng's TaskScheduler works

        log.info("Scheduling existing battles and tournaments");
        scheduleExistingBattles();
        scheduleExistingTournaments();
    }

    private void scheduleExistingTournaments() {
        // Get all tournaments from the database
        // For each tournament, schedule a task to run at the tournament's deadline
        // The task should call the battleService to start the tournament
        List<TournamentDTO> tournaments = tournamentService.getTournamentsToSchedule();
        for (TournamentDTO tournament : tournaments) {
            scheduleNewTournament(tournament);
        }

    }

    public void scheduleNewTournament(TournamentDTO tournament) {
        taskScheduler.schedule(new TournamentDeadlineHandler(tournamentService, tournament.getId()), tournament.getDeadline().toInstant(ZoneOffset.UTC));
    }

    public void scheduleNewBattle(BattleDTO battle) {
        switch (battle.getState()) {
            case BattleStateEnum.SUBSCRIPTION:
                taskScheduler.schedule(new BattleSubscriptionDeadlineHandler(battleService, battleInviteService, battle.getId(), taskScheduler), battle.getSubscriptionDeadline().toInstant(ZoneOffset.UTC));
                break;
            case ONGOING:
                taskScheduler.schedule(new BattleSubmissionDeadlineHandler(battleService, battle.getId()), battle.getSubmissionDeadline().toInstant(ZoneOffset.UTC));
                break;
            default:
                break;
        }
    }

    private void scheduleExistingBattles() {
        // Get all battles from the database
        // For each battle, schedule a task to run at the battle's deadline
        // The task should call the battleService to end the battle

        List<BattleDTO> battles = battleService.getBattlesToSchedule();
        for (BattleDTO battle : battles) {
            scheduleNewBattle(battle);
        }
    }


}
