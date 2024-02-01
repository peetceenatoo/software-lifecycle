package com.polimi.PPP.CodeKataBattle.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommitDTO {

    @NotBlank(message = "Commit hash is mandatory")
    String commitHash;

    @NotBlank(message = "Repository url is mandatory")
    String repositoryUrl;
}
