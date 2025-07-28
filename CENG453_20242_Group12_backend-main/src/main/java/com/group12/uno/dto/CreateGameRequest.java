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
@Schema(description = "Request object for creating a new game")
public class CreateGameRequest {
    @Schema(description = "Name of the game", example = "Fun Game Room")
    private String gameName;

    @Schema(description = "Maximum number of players allowed", example = "4")
    private Integer maxPlayers;
} 