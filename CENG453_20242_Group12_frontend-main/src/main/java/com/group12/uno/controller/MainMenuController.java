package com.group12.uno.controller;

import com.group12.uno.UnoApp;
import com.group12.uno.service.ApiService;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.UUID;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

public class MainMenuController {

    private VBox root;

    public MainMenuController() {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e1e1e;");

        Label title = new Label();
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", 28));
        if (ApiService.getToken() != null) {
            title.setText("UNO (Connected)");
        } else {
            title.setText("UNO (Not Connected)");
        }

        Button playButton = createMenuButton("Play Single Player", "#4caf50");
        Button playMultiplayerButton = createMenuButton("Play Multiplayer", "#2196F3");
        Button leaderboardButton = createMenuButton("Leaderboard", "#ffb400");
        Button logoutButton = createMenuButton("Logout", "#d7263d");

        playButton.setOnAction(e -> {
            GameBoardController gameBoardController = new GameBoardController("Player 1");
            Scene gameScene = new Scene(gameBoardController.getView(), 1000, 800);
            UnoApp.changeScene(gameScene, "UNO Game - Play");
        });

        playMultiplayerButton.setOnAction(e -> {
            if (ApiService.getToken() != null) {
                // Show dialog: New game or join existing
                Alert choiceDialog = new Alert(AlertType.CONFIRMATION);
                choiceDialog.setTitle("Multiplayer Game");
                choiceDialog.setHeaderText("Choose an option:");
                ButtonType newGameBtn = new ButtonType("Start New Game");
                ButtonType joinGameBtn = new ButtonType("Join Game");
                ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                choiceDialog.getButtonTypes().setAll(newGameBtn, joinGameBtn, cancelBtn);
                Optional<ButtonType> result = choiceDialog.showAndWait();
                if (result.isPresent() && result.get() == newGameBtn) {
                    // Start new game
                    try {
                        String gameId = String.valueOf(System.currentTimeMillis() % 100000000); // 8-digit number
                        String username = ApiService.getUsername();
                        List<String> playerIds = List.of(username);
                        String url = ApiService.getBaseUrl() + "/game/create?gameId=" + gameId;
                        ObjectMapper objectMapper = new ObjectMapper();
                        String body = objectMapper.writeValueAsString(playerIds);
                        Request request = Request.post(url)
                            .bodyString(body, ContentType.APPLICATION_JSON)
                            .addHeader("Authorization", "Bearer " + ApiService.getToken());
                        String response = request.execute().returnContent().asString();
                        Alert info = new Alert(AlertType.INFORMATION, "Game created! Share this code to join: " + gameId);
                        info.setHeaderText("Game Created");
                        info.showAndWait();
                        LobbyController lobbyController = new LobbyController(gameId, username);
                        Scene lobbyScene = new Scene(lobbyController.getView(), 600, 400);
                        UnoApp.changeScene(lobbyScene, "UNO Game - Lobby");
                    } catch (Exception ex) {
                        title.setText("UNO (Multiplayer Error)");
                        ex.printStackTrace();
                    }
                } else if (result.isPresent() && result.get() == joinGameBtn) {
                    // Join existing game
                    TextInputDialog inputDialog = new TextInputDialog();
                    inputDialog.setTitle("Join Game");
                    inputDialog.setHeaderText("Enter the game code:");
                    Optional<String> gameIdOpt = inputDialog.showAndWait();
                    if (gameIdOpt.isPresent() && !gameIdOpt.get().isBlank()) {
                        try {
                            String gameId = gameIdOpt.get().trim();
                            String username = ApiService.getUsername();
                            String url = ApiService.getBaseUrl() + "/game/" + gameId + "/join?playerId=" + username;
                            Request request = Request.post(url)
                                .addHeader("Authorization", "Bearer " + ApiService.getToken());
                            String response = request.execute().returnContent().asString();
                            LobbyController lobbyController = new LobbyController(gameId, username);
                            Scene lobbyScene = new Scene(lobbyController.getView(), 600, 400);
                            UnoApp.changeScene(lobbyScene, "UNO Game - Lobby");
                        } catch (Exception ex) {
                            Alert error = new Alert(AlertType.ERROR, "Failed to join game. Please check the code and try again.");
                            error.setHeaderText("Join Failed");
                            error.showAndWait();
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                title.setText("UNO (Not Connected)");
            }
        });

        leaderboardButton.setOnAction(e -> {
            LeaderboardController leaderboardController = new LeaderboardController();
            Scene leaderboardScene = new Scene(leaderboardController.getView(), 800, 500);
            UnoApp.changeScene(leaderboardScene, "UNO Game - Leaderboard");
        });

        logoutButton.setOnAction(e -> {
            ApiService.setToken(null); // Clear token on logout
            LoginController loginController = new LoginController();
            Scene loginScene = new Scene(loginController.getView(), 450, 550);
            UnoApp.changeScene(loginScene, "UNO Game - Login");
        });

        root.getChildren().addAll(title, playButton, playMultiplayerButton, leaderboardButton, logoutButton);
    }

    public VBox getView() {
        return root;
    }

    private Button createMenuButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", 18));
        button.setPrefWidth(250); // Genişlik artırıldı
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10;"
        );
        return button;
    }
}