package com.polimi.PPP.CodeKataBattle.service;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InternalErrorException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.TaskScheduling.BattleCreatedEvent;
import com.polimi.PPP.CodeKataBattle.Utilities.TimezoneUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleCreationException;
import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.BattleScore;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.Tournament;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.Utilities.URLTrimmer;
import jakarta.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.hibernate.loader.ast.spi.BatchLoader;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.springframework.web.multipart.MultipartFile;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BattleService {

    private final BattleRepository battleRepository;

    private final BattleScoreRepository battleScoreRepository;

    private final TournamentRepository tournamentRepository;

    private final GitHubAPI gitHubAPI;

    private final ModelMapper modelMapper;

    private final BattleSubscriptionRepository battleSubscriptionRepository;

    private final UserRepository userRepository;

    private final BattleInviteRepository battleInviteRepository;

    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public BattleService(BattleRepository battleRepository, BattleScoreRepository battleScoreRepository, TournamentRepository tournamentRepository,
                            GitHubAPI gitHubAPI, ModelMapper modelMapper, BattleSubscriptionRepository battleSubscriptionRepository, UserRepository userRepository,
                                BattleInviteRepository battleInviteRepository,
                                ApplicationEventPublisher eventPublisher){
        this.modelMapper = modelMapper;
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.battleScoreRepository = battleScoreRepository;
        this.tournamentRepository = tournamentRepository;
        this.gitHubAPI = gitHubAPI;
        this.battleSubscriptionRepository = battleSubscriptionRepository;
        this.battleInviteRepository = battleInviteRepository;
        this.eventPublisher = eventPublisher;


    }

    public List<BattleDTO> getBattlesByTournamentId(Long tournamentId) {

        if (tournamentRepository.findById(tournamentId).isEmpty()) {
            throw new InvalidArgumentException("Invalid tournament id");
        }

        return battleRepository.findByTournamentId(tournamentId).stream()
                               .map(battle -> modelMapper.map(battle, BattleDTO.class))
                               .collect(Collectors.toList());
    }

    public List<BattleDTO> getEnrolledBattlesByTournamentId(Long tournamentId, Long userId) {

        if (tournamentRepository.findById(tournamentId).isEmpty()) {
            throw new InvalidArgumentException("Invalid tournament id");
        }

        return battleRepository.findBattlesByTournamentIdAndUserId(tournamentId, userId).stream()
                .map(battle -> modelMapper.map(battle, BattleDTO.class))
                .collect(Collectors.toList());
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
    @Transactional
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

    public List<BattleDTO> getBattlesToSchedule() {
        return battleRepository.findByStateNotAndStateNot(BattleStateEnum.ENDED, BattleStateEnum.CONSOLIDATION).stream()
                               .map(battle -> modelMapper.map(battle, BattleDTO.class))
                               .collect(Collectors.toList());
    }

    public BattleDTO changeBattleState(Long battleId, BattleStateEnum state) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isPresent()) {
            Battle battle = battleOpt.get();

            if (battle.getState().ordinal() >= state.ordinal()) {
                throw new InvalidArgumentException("Invalid state");
            }

            battle.setState(state);

            BattleDTO toBeReturned = new BattleDTO();
            modelMapper.map(battleRepository.save(battle), toBeReturned);
            return toBeReturned;
        } else {
            throw new InvalidArgumentException("Invalid battle id");
        }
    }

    @Transactional
    public BattleDTO createBattle (Long tournamentId, BattleCreationDTO battleDTO, MultipartFile codeZip, MultipartFile testZip) throws InvalidBattleCreationException {

        // Check tournamentId is valid and tournament is ongoing
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if ( tournamentOpt.isEmpty() )
            throw new InvalidArgumentException("Invalid tournament ID.");
        Tournament tournament = tournamentOpt.get();
        if ( tournament.getState() != TournamentStateEnum.ONGOING )
            throw new InvalidBattleCreationException("Tournament is not ongoing.");

        ZonedDateTime utcSubscriptionDeadline = TimezoneUtil.convertToUtc(battleDTO.getSubscriptionDeadline());
        battleDTO.setSubscriptionDeadline(utcSubscriptionDeadline);

        ZonedDateTime utcSubmissionDeadline = TimezoneUtil.convertToUtc(battleDTO.getSubmissionDeadline());
        battleDTO.setSubmissionDeadline(utcSubmissionDeadline);

        // Validate battleDTO
        validateBattleCreation(battleDTO, tournamentId);

        // Unzip and validate codeZip and testZip
        File codeDir = unzipAndValidate(codeZip);
        File testDir = unzipAndValidate(testZip);
        if( codeDir == null || testDir == null )
            throw new InvalidBattleCreationException("Invalid zip folders.");

        // Create GitHub repositories and push contents
        String codeRepoUrl = gitHubAPI.createRepository(tournament.getName() + "-" + battleDTO.getName(), "", true);
        String testRepoUrl = gitHubAPI.createRepository(tournament.getName() + "-" + battleDTO.getName() + "-test", "", true);

        // Push the folder through the GitHubAPIs
        gitHubAPI.pushFolder(codeRepoUrl, codeDir.getAbsolutePath(), "Initial commit");
        gitHubAPI.pushFolder(testRepoUrl, testDir.getAbsolutePath(), "Initial commit");

        // Create and save Battle entity (sorry this code is not much softwareengineered)
        Battle battle = new Battle();
        modelMapper.map(battleDTO, battle);
        battle.setRepositoryLink(URLTrimmer.trimUrl(codeRepoUrl));
        battle.setTestRepositoryLink(URLTrimmer.trimUrl(testRepoUrl));
        battle.setState(BattleStateEnum.SUBSCRIPTION);
        battle.setTournament(tournament);

        // Map battleDTO to Battle and set other fields
        Battle result = battleRepository.save(battle);
        BattleDTO toBeReturned = new BattleDTO();
        modelMapper.map(result, toBeReturned);

        eventPublisher.publishEvent(new BattleCreatedEvent(this,toBeReturned));

        // Clean up temporary directories
        try {
            FileUtils.deleteDirectory(codeDir);
            FileUtils.deleteDirectory(testDir);
        }
        catch(IOException e){
            throw new InternalErrorException("File system error while extracting the zip files.");
        }

        return toBeReturned;
    }

    private void validateBattleCreation(BattleCreationDTO battleDTO, Long tournamentId) throws InvalidBattleCreationException {
        if ( battleDTO.getMinStudentsInGroup() > battleDTO.getMaxStudentsInGroup() )
            throw new InvalidBattleCreationException("Invalid Min and Max number for students groups.");
        if ( battleDTO.getSubscriptionDeadline().isAfter(battleDTO.getSubmissionDeadline()) )
            throw new InvalidBattleCreationException("Invalid deadlines.");
        if ( this.getBattlesByTournamentId(tournamentId).stream().anyMatch( (b) -> b.getName().equals(battleDTO.getName()) ) )
            throw new InvalidBattleCreationException("Invalid name for the Battle.");
    }

    private File unzipAndValidate(MultipartFile zipFile) throws InvalidBattleCreationException {

        try ( InputStream inputStream = zipFile.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            // Create a temporary directory to extract the contents
            File tempDirectory = Files.createTempDirectory("tempDir").toFile();

            ZipEntry entry;
            boolean hasSrcDirectory = false;
            boolean hasPomFile = false;

            while ( ( entry = zipInputStream.getNextEntry() ) != null ) {
                String entryName = entry.getName();
                File entryFile = new File(tempDirectory, entryName);

                if ( entry.isDirectory() ) {
                    // Check if the directory is named "src"
                    if (entryName.equals("src/"))
                        hasSrcDirectory = true;
                } else {
                    // Check if the file is named "pom.xml"
                    if (entryName.equals("pom.xml"))
                        hasPomFile = true;

                    try ( OutputStream outputStream = new FileOutputStream(entryFile) ) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ( (bytesRead = zipInputStream.read(buffer)) != -1 )
                            outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }

            // Check if both conditions are met: "src" directory and "pom.xml" file exist
            if ( hasSrcDirectory && hasPomFile )
                return tempDirectory;
            else {
                // Cleanup and delete the temporary directory if validation fails
                deleteDirectory(tempDirectory);
                return null;
            }
        } catch ( Exception e ){
            throw new InternalErrorException("File system error while extracting the zip files.");
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if ( files != null ) {
            for ( File file : files ) {
                if ( file.isDirectory() )
                    deleteDirectory(file);
                else
                    file.delete();
            }
        }
        directory.delete();
    }
}
