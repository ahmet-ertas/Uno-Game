package com.group12.uno.dto;

import com.group12.uno.model.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing game information")
public class GameResponse {
    @Schema(description = "Unique identifier of the game")
    private Long id;

    @Schema(description = "Name of the game")
    private String gameName;

    @Schema(description = "Maximum number of players allowed")
    private Integer maxPlayers;

    @Schema(description = "Current number of players in the game")
    private Integer currentPlayers;

    @Schema(description = "Current status of the game (WAITING, IN_PROGRESS, FINISHED)")
    private Game.GameStatus status;

    @Schema(description = "Username of the game creator")
    private String creatorUsername;

    @Schema(description = "When the game was created")
    private LocalDateTime createdAt;

    @Schema(description = "List of players in the game with their scores")
    private List<PlayerScore> players;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Player information with score")
    public static class PlayerScore {
        @Schema(description = "Username of the player")
        private String username;

        @Schema(description = "Current score of the player")
        private Integer score;
    }
} 