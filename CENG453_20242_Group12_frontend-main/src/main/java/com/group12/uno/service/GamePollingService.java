package com.group12.uno.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

public class GamePollingService {
    private static GamePollingService instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Consumer<Map<String, Object>> onUpdateReceived;
    private String gameId;
    private String lastUpdateTimestamp;
    private boolean isPolling = false;

    private GamePollingService() {}

    public static synchronized GamePollingService getInstance() {
        if (instance == null) {
            instance = new GamePollingService();
        }
        return instance;
    }

    public void startPolling(String gameId, Consumer<Map<String, Object>> onUpdateReceived) {
        this.gameId = gameId;
        this.onUpdateReceived = onUpdateReceived;
        this.isPolling = true;
        this.lastUpdateTimestamp = String.valueOf(System.currentTimeMillis());

        // Start polling every 2 seconds
        scheduler.scheduleAtFixedRate(this::pollForUpdates, 0, 2, TimeUnit.SECONDS);
    }

    private void pollForUpdates() {
        if (!isPolling) return;

        try {
            // Call the backend API to get updates
            String url = ApiService.getBaseUrl() + String.format("/api/game/%s/updates?since=%s", gameId, lastUpdateTimestamp);
            Request request = Request.get(url);
            
            if (ApiService.getToken() != null) {
                request.addHeader("Authorization", "Bearer " + ApiService.getToken());
            }

            String response = request.execute()
                .returnContent()
                .asString();

            if (response != null && !response.isEmpty()) {
                Map<String, Object> updates = objectMapper.readValue(response, new TypeReference<>() {});
                
                // Update the timestamp
                if (updates.containsKey("timestamp")) {
                    lastUpdateTimestamp = updates.get("timestamp").toString();
                }

                // Process updates on the JavaFX thread
                Platform.runLater(() -> {
                    if (onUpdateReceived != null) {
                        onUpdateReceived.accept(updates);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error polling for updates: " + e.getMessage());
        }
    }

    public void sendGameAction(String actionType, Object actionData) {
        try {
            Map<String, Object> action = Map.of(
                "type", actionType,
                "gameId", gameId,
                "data", actionData
            );

            String url = ApiService.getBaseUrl() + "/api/game/action";
            Request request = Request.post(url)
                .bodyString(objectMapper.writeValueAsString(action), ContentType.APPLICATION_JSON);

            if (ApiService.getToken() != null) {
                request.addHeader("Authorization", "Bearer " + ApiService.getToken());
            }

            request.execute();
        } catch (Exception e) {
            System.err.println("Error sending game action: " + e.getMessage());
        }
    }

    public void stopPolling() {
        isPolling = false;
        scheduler.shutdown();
    }

    public boolean isPolling() {
        return isPolling;
    }
} 