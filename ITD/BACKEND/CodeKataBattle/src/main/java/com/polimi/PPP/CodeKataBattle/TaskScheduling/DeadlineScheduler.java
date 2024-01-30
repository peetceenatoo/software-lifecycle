package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.TournamentDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.Utilities.IGitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.TimezoneUtil;
import com.polimi.PPP.CodeKataBattle.service.BattleInviteService;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class DeadlineScheduler {
    private final TaskScheduler taskScheduler;
    private final BattleService battleService;
    private final BattleInviteService battleInviteService;
    private final TournamentService tournamentService;
    private final IGitHubAPI gitHubAPI;

    @Autowired
    public DeadlineScheduler(TaskScheduler taskScheduler, BattleService battleService, TournamentService tournamentService, BattleInviteService battleInviteService, IGitHubAPI gitHubAPI) {
        this.battleService = battleService;
        this.tournamentService = tournamentService;
        this.taskScheduler = taskScheduler;
        this.battleInviteService = battleInviteService;
        this.gitHubAPI = gitHubAPI;
    }

    @PostConstruct
    public void init(){

        //TODO: we should correctly handle changes of deadlines and reschedule the tasks
        // .
        // Immediate processing of past deadlines after a crash/restart happens automatically thanks to
        // how Spring's TaskScheduler works

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

    @EventListener
    public void handleTournamentCreated(TournamentCreatedEvent event) {
        TournamentDTO tournament = event.getTournament();
        // Logic to schedule tasks for the created tournament
        // You might retrieve additional details using the tournamentId
        // and then schedule the task
        scheduleNewTournament(tournament);
    }

    @EventListener
    public void handleBattleCreated(BattleCreatedEvent event) {
        BattleDTO battle = event.getBattle();
        // Logic to schedule tasks for the created battle
        // You might retrieve additional details using the battleId
        // and then schedule the task
        scheduleNewBattle(battle);
    }



    private void scheduleNewTournament(TournamentDTO tournament) {

        ZonedDateTime deadlineZoned = TimezoneUtil.convertUtcToLocalTime(tournament.getDeadline());
        ZonedDateTime currentDateTime = TimezoneUtil.convertUtcToLocalTime(ZonedDateTime.now(ZoneOffset.UTC));

        log.info("Current LocalDateTime: " + currentDateTime);
        log.info("Scheduling Tournament closing at : " + deadlineZoned);

        taskScheduler.schedule(new TournamentDeadlineHandler(tournamentService, tournament.getId()), deadlineZoned.toInstant());
    }

    private void scheduleNewBattle(BattleDTO battle) {
        ZonedDateTime currentDateTime = TimezoneUtil.convertUtcToLocalTime(ZonedDateTime.now(ZoneOffset.UTC));

        log.info("Current LocalDateTime: " + currentDateTime);

        ZonedDateTime deadlineZoned;

        switch (battle.getState()) {
            case SUBSCRIPTION:
                deadlineZoned = TimezoneUtil.convertUtcToLocalTime(battle.getSubscriptionDeadline());
                log.info("Scheduling Tournament closing at : " + deadlineZoned);
                taskScheduler.schedule(new BattleSubscriptionDeadlineHandler(battleService, battleInviteService, battle.getId(), taskScheduler, gitHubAPI), deadlineZoned.toInstant());

                break;
            case ONGOING:
                deadlineZoned = TimezoneUtil.convertUtcToLocalTime(battle.getSubmissionDeadline());
                log.info("Scheduling Tournament closing at : " + deadlineZoned);
                taskScheduler.schedule(new BattleSubmissionDeadlineHandler(battleService, battle.getId()), deadlineZoned.toInstant());
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
