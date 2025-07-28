package com.group12.uno.controller;

import com.group12.uno.UnoApp;
import com.group12.uno.service.ApiService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.hc.client5.http.fluent.Request;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LobbyController {
    private final VBox root;
    private final String gameId;
    private final String username;
    private final Label playersLabel;
    private final Button startButton;
    private final Timer timer = new Timer(true);
    private boolean isHost = false;
    private boolean gameStarted = false;

    public LobbyController(String gameId, String username) {
        this.gameId = gameId;
        this.username = username;
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #222;");

        Label title = new Label("UNO Lobby: " + gameId);
        title.setFont(Font.font("Verdana", 28));
        title.setStyle("-fx-text-fill: white;");

        playersLabel = new Label();
        playersLabel.setFont(Font.font("Arial", 18));
        playersLabel.setStyle("-fx-text-fill: white;");

        startButton = new Button("Start Game");
        startButton.setFont(Font.font("Arial", 18));
        startButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        startButton.setOnAction(e -> startGame());
        startButton.setVisible(false);

        root.getChildren().addAll(title, playersLabel, startButton);

        pollLobby();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> pollLobby());
            }
        }, 2000, 2000);
    }

    private void pollLobby() {
        try {
            String url = ApiService.getBaseUrl() + "/game/" + gameId + "/lobby";
            Request request = Request.get(url)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            String response = request.execute().returnContent().asString();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> lobby = objectMapper.readValue(response, new TypeReference<>() {});
            List<String> players = (List<String>) lobby.get("players");
            boolean started = (Boolean) lobby.get("started");
            StringBuilder sb = new StringBuilder("Players in lobby:\n");
            for (String p : players) {
                sb.append("- ").append(p);
                if (p.equals(username)) sb.append(" (You)");
                sb.append("\n");
            }
            playersLabel.setText(sb.toString());
            isHost = players.size() > 0 && players.get(0).equals(username);
            startButton.setVisible(isHost && players.size() > 1 && !started);
            if (started && !gameStarted) {
                // Transition to game
                timer.cancel();
                MultiplayerGameBoardController gameBoardController = new MultiplayerGameBoardController(gameId, username);
                Scene gameScene = new Scene(gameBoardController.getView(), 1000, 800);
                UnoApp.changeScene(gameScene, "UNO Game - Multiplayer");
                gameStarted = true;
            }
        } catch (Exception e) {
            playersLabel.setText("Error polling lobby: " + e.getMessage());
        }
    }

    private void startGame() {
        try {
            String url = ApiService.getBaseUrl() + "/game/" + gameId + "/start";
            Request request = Request.post(url)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            String response = request.execute().returnContent().asString();
            // Game will start for all via polling
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to start game: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public VBox getView() {
        return root;
    }
} 