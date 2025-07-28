package com.group12.uno.model;

public class LeaderboardEntry {
    private String username;
    private int totalScore;
    private int gamesPlayed;
    private int gamesWon;
    private double winRate;

    // Gerekli: Jackson'ın kullanabilmesi için boş constructor
    public LeaderboardEntry() {}

    public String getUsername() {
        return username;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public double getWinRate() {
        return winRate;
    }
}
