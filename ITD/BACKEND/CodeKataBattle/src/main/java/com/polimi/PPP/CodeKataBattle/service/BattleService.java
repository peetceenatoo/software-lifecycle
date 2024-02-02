package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InternalErrorException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Exceptions.PendingSubmissionsException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.TaskScheduling.BattleCreatedEvent;
import com.polimi.PPP.CodeKataBattle.TaskScheduling.TournamentCreatedEvent;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import com.polimi.PPP.CodeKataBattle.Utilities.TimezoneUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleCreationException;
import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.Tournament;
import com.polimi.PPP.CodeKataBattle.Utilities.URLTrimmer;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

    private final SubmissionRepository submissionRepository;

    private final UserRepository userRepository;

    private final BattleInviteRepository battleInviteRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Qualifier("emailProvider")
    private NotificationProvider notificationProvider;


    @Autowired
    public BattleService(BattleRepository battleRepository, BattleScoreRepository battleScoreRepository, TournamentRepository tournamentRepository,
                            GitHubAPI gitHubAPI, ModelMapper modelMapper, BattleSubscriptionRepository battleSubscriptionRepository, UserRepository userRepository,
                                BattleInviteRepository battleInviteRepository,
                                ApplicationEventPublisher eventPublisher,
                                NotificationProvider notificationProvider,
                                SubmissionRepository submissionRepository) {
        this.modelMapper = modelMapper;
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.battleScoreRepository = battleScoreRepository;
        this.tournamentRepository = tournamentRepository;
        this.gitHubAPI = gitHubAPI;
        this.battleSubscriptionRepository = battleSubscriptionRepository;
        this.battleInviteRepository = battleInviteRepository;
        this.eventPublisher = eventPublisher;
        this.notificationProvider = notificationProvider;
        this.submissionRepository = submissionRepository;


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

    private boolean userHasPermissionToBattle(UserDTO user, Long battleId) {
        if (user.getRole().getName() == RoleEnum.ROLE_EDUCATOR) {
            Optional<Battle> battle = battleRepository.findById(battleId);
            if (battle.isEmpty()) {
                if (battle.get().getTournament().getUsers().stream().anyMatch(user1 -> user1.getId().equals(user.getId())))
                    return false;
            }
        } else {

            boolean isUserSubscribed = battleSubscriptionRepository.existsByBattleIdAndUserId(battleId, user.getId());
            if (!isUserSubscribed) {
                return false;
            }
        }
        return true;
    }

    public BattleDTO closeBattle(Long battleId, UserDTO user) {
        if (!userHasPermissionToBattle(user, battleId)) {
            throw new InvalidArgumentException("User not an educator of the battle");
        }
        Optional<Battle> battleOpt = battleRepository.findById(battleId);

        if(battleOpt.isEmpty()){
            throw new InvalidArgumentException("Battle not found");
        }

        Battle battle = battleOpt.get();

        List<Submission> submissions = submissionRepository.findByBattleId(battleId);

        if(submissions.stream().anyMatch(submission -> submission.getState() == SubmissionStateEnum.PENDING)){
            throw new PendingSubmissionsException("There are still submissions to be evaluated");
        }

        battle.setState(BattleStateEnum.ENDED);
        Battle toBeReturned = battleRepository.save(battle);

        List<User> users = battleSubscriptionRepository.findUsersByBattleId(toBeReturned.getId());
        MessageDTO messageDTO = new MessageDTO("The battle '" + toBeReturned.getName() + "' of the tournament '" + toBeReturned.getTournament().getName() + "' has been closed.", "Battle closed");
        notificationProvider.sendNotification(messageDTO, users.stream().filter(s -> s.getRole().getName() == RoleEnum.ROLE_STUDENT).map(User::getEmail).toList());

        return modelMapper.map(toBeReturned, BattleDTO.class);
    }

    @Transactional
    public List<BattleRankingDTO> getBattleRanking(Long battleId, UserDTO user) {
        if (!userHasPermissionToBattle(user, battleId)) {
            throw new InvalidArgumentException("User not enrolled in the battle");
        }
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


        if (battleDTO.getMinStudentsInGroup() < 1 || battleDTO.getMaxStudentsInGroup() < 1)
            throw new InvalidBattleCreationException("Invalid Min and Max number for students groups.");

        if(battleDTO.getMinStudentsInGroup() > battleDTO.getMaxStudentsInGroup())
            throw new InvalidBattleCreationException("Invalid Min and Max number for students groups.");

        if(battleDTO.getSubscriptionDeadline().isAfter(battleDTO.getSubmissionDeadline()))
            throw new InvalidBattleCreationException("Invalid deadlines.");


        // Check tournamentId is valid and tournament is ongoing
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if ( tournamentOpt.isEmpty() )
            throw new InvalidArgumentException("Invalid tournament ID.");
        Tournament tournament = tournamentOpt.get();
        if ( tournament.getState() != TournamentStateEnum.ONGOING )
            throw new InvalidBattleCreationException("Tournament is not ongoing.");

        // Check if a battle with the same name in this tournament already exists
        if ( battleRepository.existsByTournamentIdAndName(tournamentId, battleDTO.getName()) )
            throw new InvalidBattleCreationException("A battle with the same name already exists in this tournament.");

        // Convert deadlines to UTC
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
        codeRepoUrl = URLTrimmer.extractRepoPath(codeRepoUrl);


        String testRepoUrl;
        try{
            testRepoUrl = gitHubAPI.createRepository(tournament.getName() + "-" + battleDTO.getName() + "-test", "", true);
        }catch (Exception e) {
            try {
                gitHubAPI.deleteRepository(codeRepoUrl);
            } catch (Exception e2) {
                //do nothing
            }
            throw e;
        }


        testRepoUrl = URLTrimmer.extractRepoPath(testRepoUrl);

        Battle result;

        try{
            // Push the folder through the GitHubAPIs
            gitHubAPI.pushFolder(codeRepoUrl, codeDir.getAbsolutePath(), "Initial commit");
            gitHubAPI.pushFolder(testRepoUrl, testDir.getAbsolutePath(), "Initial commit");

            // Create and save Battle entity
            Battle battle = new Battle();
            modelMapper.map(battleDTO, battle);
            battle.setRepositoryLink(codeRepoUrl);
            battle.setTestRepositoryLink(testRepoUrl);
            battle.setState(BattleStateEnum.SUBSCRIPTION);
            battle.setTournament(tournament);


            // Map battleDTO to Battle and set other fields
            result = battleRepository.save(battle);
        }catch (Exception e){
            try{
                gitHubAPI.deleteRepository(codeRepoUrl);
                gitHubAPI.deleteRepository(testRepoUrl);
            }catch (Exception e2){
                //do nothing
            }
            throw e;
        }

        BattleDTO toBeReturned = new BattleDTO();
        modelMapper.map(result, toBeReturned);



        // Clean up temporary directories
        try {
            FileUtils.deleteDirectory(codeDir);
            FileUtils.deleteDirectory(testDir);
        }
        catch(IOException e){
            throw new InternalErrorException("File system error while extracting the zip files.");
        }

        // Register an after-commit lambda
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {

                eventPublisher.publishEvent(new BattleCreatedEvent(this,toBeReturned));

                // Send notification to all enrolled students
                List<String> studentsEmail = tournament.getUsers().stream().filter(s -> s.getRole().getName() == RoleEnum.ROLE_STUDENT).map(User::getEmail).toList();
                MessageDTO messageDTO = new MessageDTO("The new battle '" + result.getName() + "' of the tournament '" + tournament.getName() + "' has been created, check it out.", "New battle created");
                notificationProvider.sendNotification(messageDTO, studentsEmail);
            }

            // Implement other methods as needed or leave them as default
        });

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
        File tempZipFile = null;
        try {
            // Store the multipart file in a temporary file
            tempZipFile = Files.createTempFile("uploadedZip", ".zip").toFile();
            zipFile.transferTo(tempZipFile);

            try (InputStream inputStream = new FileInputStream(tempZipFile);
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

                File tempDirectory = Files.createTempDirectory("tempDir").toFile();

                ZipEntry entry;
                boolean hasSrcDirectory = false;
                boolean hasPomFile = false;

                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    File entryFile = new File(tempDirectory, entryName);

                    // Create parent directories for the entry file if they don't exist
                    if (entry.isDirectory()) {
                        entryFile.mkdirs();
                        if (entryName.equals("src/"))
                            hasSrcDirectory = true;
                    } else {
                        File parentDir = entryFile.getParentFile();
                        if (parentDir != null && !parentDir.exists()) {
                            parentDir.mkdirs();
                        }

                        if(entryName.contains(".DS_Store")) continue;
                        if (entryName.equals("pom.xml"))
                            hasPomFile = true;

                        try (OutputStream outputStream = new FileOutputStream(entryFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }

                if (hasSrcDirectory && hasPomFile)
                    return tempDirectory;
                else {
                    deleteDirectory(tempDirectory);
                    return null;
                }
            }
        } catch (Exception e) {
            throw new InternalErrorException("File system error while extracting the zip files.");
        } finally {
            // Delete the temporary zip file
            if (tempZipFile != null && tempZipFile.exists()) {
                tempZipFile.delete();
            }
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
