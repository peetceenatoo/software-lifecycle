package com.polimi.PPP.CodeKataBattle.Controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polimi.PPP.CodeKataBattle.DTOs.TournamentDTO;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TournamentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentController tournamentController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController).build();
    }

    // JSON helper
    private String toJson(Object obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    @Test
    public void testGetTournament() throws Exception {
        Long tournamentId = 1L;
        TournamentDTO mockTournament = new TournamentDTO(); // Assume TournamentDTO is a valid DTO for your tournament
        // Configure Mock behavior
        when(tournamentService.getTournamentById(tournamentId)).thenReturn(mockTournament);

        mockMvc.perform(get("/api/tournaments/" + tournamentId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(mockTournament)));

        verify(tournamentService).getTournamentById(tournamentId);
    }


    // ... Test methods will be here ...
}
