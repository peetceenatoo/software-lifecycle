package com.polimi.PPP.CodeKataBattle.service;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InternalErrorException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleCreationException;
import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.BattleScore;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.Tournament;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.Utilities.URLTrimmer;
import jakarta.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private BattleScoreRepository battleScoreRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private GitHubAPI gitHubAPI;

    @Autowired
    private ModelMapper modelMapper;

    public List<BattleDTO> getBattlesByTournamentId(Long tournamentId) {
        return battleRepository.findByTournamentId(tournamentId).stream()
                               .map(battle -> modelMapper.map(battle, BattleDTO.class))
                               .collect(Collectors.toList());
    }

    public List<BattleDTO> getEnrolledBattlesByTournamentId(Long tournamentId, Long userId) {
        return battleRepository.findBattlesByTournamentIdAndUserId(tournamentId, userId).stream()
                .map(battle -> modelMapper.map(battle, BattleDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<String> correctScore(Long submissionId, int correction) {
        Optional<BattleScore> battleScoreOpt = battleScoreRepository.findBySubmissionId(submissionId);

        if (battleScoreOpt.isPresent()) {
            BattleScore battleScore = battleScoreOpt.get();
            battleScore.setManualCorrection(correction);
            battleScoreRepository.save(battleScore);
            return Optional.of("Success");
        } else {
            throw new InvalidArgumentException("Invalid submission id");
        }
    }

    public List<BattleDTO> getBattlesToSchedule() {
        return battleRepository.findByStateNotAndStateNot(BattleStateEnum.ENDED, BattleStateEnum.CONSOLIDATION).stream()
                               .map(battle -> modelMapper.map(battle, BattleDTO.class))
                               .collect(Collectors.toList());
    }

    public BattleDTO getBattleById(Long battleId) {
        return modelMapper.map(battleRepository.findById(battleId).orElseThrow(), BattleDTO.class);
    }

    public void changeBattleState(Long battleId, BattleStateEnum state) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isPresent()) {
            Battle battle = battleOpt.get();

            if (battle.getState().ordinal() >= state.ordinal()) {
                throw new InvalidArgumentException("Invalid state");
            }

            battle.setState(state);
            battleRepository.save(battle);
        } else {
            throw new InvalidArgumentException("Invalid battle id");
        }
    }

    @Transactional
    public void startBattle(Long battleId) {

        // set all pending invites for this battle to rejected

        changeBattleState(battleId, BattleStateEnum.ONGOING);
    }

    @Transactional
    public void createBattle (Long tournamentId, BattleCreationDTO battleDTO, MultipartFile codeZip, MultipartFile testZip) throws InvalidBattleCreationException {
        // Validate battleDTO
        validateBattleCreation(battleDTO, tournamentId);

        // Unzip and validate codeZip and testZip
        File codeDir = unzipAndValidate(codeZip);
        File testDir = unzipAndValidate(testZip);
        if( codeDir == null || testDir == null )
            throw new InvalidBattleCreationException("Invalid zip folders.");

        // Get tournament by ID
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if( tournamentOptional.isEmpty() )
            throw new InvalidArgumentException("Invalid tournament ID.");
        Tournament tournament = tournamentOptional.get();

        // Create GitHub repositories and push contents
        String codeRepoUrl = gitHubAPI.createRepository(tournament.getName() + "-" + battleDTO.getName(), "", true);
        String testRepoUrl = gitHubAPI.createRepository(tournament.getName() + "-" + battleDTO.getName() + "-test", "", true);

        // Push the folder through the GitHubAPIs
        gitHubAPI.pushFolder(codeRepoUrl, codeDir.getAbsolutePath(), "Initial commit");
        gitHubAPI.pushFolder(testRepoUrl, testDir.getAbsolutePath(), "Initial commit");

        // Create and save Battle entity (sorry this code is not much softwareengineered)
        // I will surely fix it later on
        Battle battle = new Battle();
        battle.setName(battleDTO.getName());
        battle.setState(BattleStateEnum.SUBSCRIPTION);
        battle.setManualScoringRequires(battleDTO.isManualScoringRequires());
        battle.setProgrammingLanguage(battleDTO.getProgrammingLanguage());
        battle.setRepositoryLink(URLTrimmer.trimUrl(codeRepoUrl));
        battle.setTestRepositoryLink(URLTrimmer.trimUrl(testRepoUrl));
        battle.setMaxStudentsInGroup(battleDTO.getMaxStudentsInGroup());
        battle.setMinStudentsInGroup(battleDTO.getMinStudentsInGroup());
        battle.setSubscribtion_deadline(battleDTO.getSubscriptionDeadline());
        battle.setSubmissiondeadline(battleDTO.getSubmissionDeadline());
        battle.setTournament(tournament);

        // Map battleDTO to Battle and set other fields
        battleRepository.save(battle);

        // Clean up temporary directories
        try {
            FileUtils.deleteDirectory(codeDir);
            FileUtils.deleteDirectory(testDir);
        }
        catch(IOException e){
            throw new InternalErrorException("File system error while extracting the zip files.");
        }
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
                    if (entryName.equals("src"))
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
