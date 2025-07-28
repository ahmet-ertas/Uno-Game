package com.group12.uno.controller;

import com.group12.uno.service.GamePollingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@Tag(name = "Game Polling", description = "Game polling endpoints for real-time updates")
@SecurityRequirement(name = "bearerAuth")
public class GamePollingController {

    private final GamePollingService gamePollingService;

    @Autowired
    public GamePollingController(GamePollingService gamePollingService) {
        this.gamePollingService = gamePollingService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new game")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> createGame(
            @Parameter(description = "Game ID") @RequestParam String gameId,
            @Parameter(description = "List of player IDs") @RequestBody List<String> playerIds) {
        return ResponseEntity.ok(gamePollingService.createGame(gameId, playerIds));
    }

    @GetMapping("/{gameId}/updates")
    @Operation(summary = "Get game updates since last timestamp")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved updates",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> getUpdates(
            @Parameter(description = "Game ID") @PathVariable String gameId,
            @Parameter(description = "Timestamp of last update") @RequestParam String since) {
        return ResponseEntity.ok(gamePollingService.getUpdates(gameId, since));
    }

    @PostMapping("/action")
    @Operation(summary = "Send a game action")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Action processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid action"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> processAction(
            @Parameter(description = "Game action") @RequestBody Map<String, Object> action) {
        return ResponseEntity.ok(gamePollingService.processAction(action));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable String gameId, @RequestParam String playerId) {
        System.out.println("Join request: gameId=" + gameId + ", playerId=" + playerId);
        return ResponseEntity.ok(gamePollingService.joinGame(gameId, playerId));
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<Map<String, Object>> startGame(@PathVariable String gameId) {
        return ResponseEntity.ok(gamePollingService.startGame(gameId));
    }

    @GetMapping("/{gameId}/lobby")
    public ResponseEntity<Map<String, Object>> getLobby(@PathVariable String gameId) {
        return ResponseEntity.ok(gamePollingService.getLobby(gameId));
    }
} 