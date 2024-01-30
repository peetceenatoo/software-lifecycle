package com.polimi.PPP.CodeKataBattle.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotNull(message = "Registration deadline is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;

    @NotNull(message = "Tournament invitations is mandatory")
    private List<Long> educatorsInvited;
    // Getters and setters
}
