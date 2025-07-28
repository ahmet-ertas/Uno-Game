package com.group12.uno.controller;

import com.group12.uno.dto.LeaderboardEntry;
import com.group12.uno.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Leaderboard APIs")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Get weekly leaderboard", description = "Retrieves the leaderboard for the current week")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved weekly leaderboard",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaderboardEntry.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/weekly")
    public ResponseEntity<List<LeaderboardEntry>> getWeeklyLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard());
    }

    @Operation(summary = "Get monthly leaderboard", description = "Retrieves the leaderboard for the current month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly leaderboard",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaderboardEntry.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/monthly")
    public ResponseEntity<List<LeaderboardEntry>> getMonthlyLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getMonthlyLeaderboard());
    }

    @Operation(summary = "Get all-time leaderboard", description = "Retrieves the all-time leaderboard")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all-time leaderboard",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaderboardEntry.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/all-time")
    public ResponseEntity<List<LeaderboardEntry>> getAllTimeLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getAllTimeLeaderboard());
    }
} 