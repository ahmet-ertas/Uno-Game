package com.group12.uno.controller;

import com.group12.uno.dto.CreateGameRequest;
import com.group12.uno.dto.GameResponse;
import com.group12.uno.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Tag(name = "Game", description = "Game management APIs")
@SecurityRequirement(name = "bearerAuth")
public class GameController {

    private final GameService gameService;

    @Operation(summary = "Create a new game", description = "Creates a new game room and returns the game details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game created successfully",
                content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @RequestBody CreateGameRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(gameService.createGame(request, authentication.getName()));
    }

    /*
    @Operation(summary = "Join a game", description = "Joins an existing game room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined the game",
                content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "400", description = "Game is full or already started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameResponse> joinGame(
            @Parameter(description = "ID of the game to join") @PathVariable Long gameId,
            Authentication authentication) {
        return ResponseEntity.ok(gameService.joinGame(gameId, authentication.getName()));
    }
    */

    /*
    @Operation(summary = "Start a game", description = "Starts a game that has enough players")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game started successfully",
                content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "400", description = "Not enough players or game already started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Only the creator can start the game"),
        @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameResponse> startGame(
            @Parameter(description = "ID of the game to start") @PathVariable Long gameId,
            Authentication authentication) {
        return ResponseEntity.ok(gameService.startGame(gameId, authentication.getName()));
    }
    */

    @Operation(summary = "Get game state", description = "Retrieves the current state of a game")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved game state",
                content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a player in this game"),
        @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGameState(
            @Parameter(description = "ID of the game to get state for") @PathVariable Long gameId,
            Authentication authentication) {
        return ResponseEntity.ok(gameService.getGameState(gameId, authentication.getName()));
    }
}