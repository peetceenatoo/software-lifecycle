package com.polimi.PPP.CodeKataBattle.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentCreationDTO {

    @NotBlank(message = "Tournament name is mandatory")
    private String tournamentName;

    @NotNull(message = "Tournament deadline is mandatory")
    private LocalDateTime registrationDeadline;

    @NotNull(message = "Tournament invitations is mandatory")
    private List<Long> educatorsInvited;
    // Getters and setters
}
