package com.group12.uno.repository;

import com.group12.uno.dto.LeaderboardEntry;
import com.group12.uno.model.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    @Query("SELECT new com.group12.uno.dto.LeaderboardEntry(" +
            "gp.player.username, " +
            "SUM(gp.score), " +
            "COUNT(DISTINCT gp.game), " +
            "COUNT(DISTINCT CASE WHEN gp.score > 0 THEN gp.game END), " +
            "CAST(COUNT(DISTINCT CASE WHEN gp.score > 0 THEN gp.game END) AS double) / " +
            "CAST(COUNT(DISTINCT gp.game) AS double) * 100.0) " +
            "FROM GamePlayer gp " +
            "WHERE (:startDate IS NULL OR gp.joinedAt >= :startDate) " +
            "GROUP BY gp.player.username " +
            "ORDER BY SUM(gp.score) DESC")
    List<LeaderboardEntry> findLeaderboardEntries(@Param("startDate") LocalDateTime startDate);
} 