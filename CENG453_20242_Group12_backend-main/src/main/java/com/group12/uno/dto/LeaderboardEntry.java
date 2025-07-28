package com.group12.uno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a player's entry in the leaderboard")
public class LeaderboardEntry {
    @Schema(description = "Username of the player")
    private String username;

    @Schema(description = "Total score of the player")
    private long totalScore;

    @Schema(description = "Total number of games played")
    private long gamesPlayed;

    @Schema(description = "Number of games won")
    private long gamesWon;

    @Schema(description = "Win rate percentage")
    private double winRate;

    public long getTotalScore() {
        return totalScore;
    }
}