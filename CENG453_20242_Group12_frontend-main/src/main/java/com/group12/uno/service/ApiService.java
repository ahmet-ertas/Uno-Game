package com.group12.uno.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group12.uno.model.LeaderboardEntry;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.util.List;
import java.util.Map;

public class ApiService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String jwtToken = null;
    private static String username = null;

    // **** YENİ METOD ****
    public static String getBaseUrl() {
        return BASE_URL;
    }
    // *********************

    public static String getToken() {
        return jwtToken;
    }

    public static void setToken(String token) {
        jwtToken = token;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String user) {
        username = user;
    }

    public static boolean login(String username, String password) {
        try {
            String jsonBody = objectMapper.writeValueAsString(new LoginRequest(username, password));
            String response = Request.post(BASE_URL + "/auth/login")
                    .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnContent()
                    .asString();

            Map<String, Object> jsonMap = objectMapper.readValue(response, new TypeReference<>() {});
            if (jsonMap.containsKey("token")) {
                setToken((String) jsonMap.get("token"));
                setUsername(username);
                System.out.println("JWT Token: " + getToken());
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean register(String username, String password) {
        try {
            String jsonBody = objectMapper.writeValueAsString(new RegisterRequest(username, password));
            String response = Request.post(BASE_URL + "/auth/register")
                    .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnContent()
                    .asString();

            System.out.println("Register response: " + response);
            if (response.contains("token")) {
                setUsername(username);
                System.out.println("JWT Token: " + getToken());
            }
            return response.contains("token") || response.contains("created");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // DTO'lar
    public static class LoginRequest {
        public String username;
        public String password;
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public RegisterRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    public static List<LeaderboardEntry> getLeaderboard(String type) {
        try {
            String url = BASE_URL + "/leaderboard/" + type;
            org.apache.hc.client5.http.fluent.Request request = Request.get(url);

            if (jwtToken != null) {
                request.addHeader("Authorization", "Bearer " + jwtToken);
            }

            String response = request.execute()
                    .returnContent()
                    .asString();

            return objectMapper.readValue(
                    response,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LeaderboardEntry.class)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static String createGame() {
        try {
            Map<String, Object> request = Map.of(
                "maxPlayers", 2,
                "noCPU", true,
                "gameMode", "MULTIPLAYER_ONLY"
            );
            
            String jsonBody = objectMapper.writeValueAsString(request);
            String response = Request.post(BASE_URL + "/game/create")
                    .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .execute()
                    .returnContent()
                    .asString();

            Map<String, Object> jsonMap = objectMapper.readValue(response, new TypeReference<>() {});
            return (String) jsonMap.get("gameId");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean joinGame(String gameId) {
        try {
            Map<String, Object> request = Map.of(
                "gameId", gameId,
                "username", username
            );
            
            String jsonBody = objectMapper.writeValueAsString(request);
            String response = Request.post(BASE_URL + "/game/join")
                    .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .execute()
                    .returnContent()
                    .asString();

            return response.contains("joined");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startGame(String gameId) {
        try {
            Map<String, Object> request = Map.of(
                "gameId", gameId,
                "maxPlayers", 2,
                "noCPU", true,
                "gameMode", "MULTIPLAYER_ONLY",
                "waitForPlayers", true
            );
            
            String jsonBody = objectMapper.writeValueAsString(request);
            String response = Request.post(BASE_URL + "/game/start")
                    .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .execute()
                    .returnContent()
                    .asString();

            return response.contains("started");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}