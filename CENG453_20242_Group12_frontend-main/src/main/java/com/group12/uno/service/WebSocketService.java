package com.group12.uno.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;

public class WebSocketService {

    private WebSocketClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String serverUrl; // Örn: wss://ceng453-20242-group12-backend.onrender.com/game
    private Consumer<Map<String, Object>> onMessageReceived; // Gelen mesajları işleyecek bir lambda

    // Singleton pattern için basit bir implementasyon
    private static WebSocketService instance;

    private WebSocketService() {
        // Backend URL'nizi ApiService'deki BASE_URL'den türetebilirsiniz
        // HTTP/HTTPS'i WS/WSS ile değiştirin ve /api kısmını kaldırıp /game ekleyin
        // Örnek: "https://ceng453-20242-group12-backend.onrender.com/api" -> "wss://ceng453-20242-group12-backend.onrender.com/game"
        String baseHttpUrl = ApiService.getBaseUrl().replace("/api", ""); // ApiService'den BASE_URL'i alıp /api'yi kaldırıyoruz
        if (baseHttpUrl.startsWith("https://")) {
            this.serverUrl = baseHttpUrl.replace("https://", "wss://") + "/game";
        } else if (baseHttpUrl.startsWith("http://")) {
            this.serverUrl = baseHttpUrl.replace("http://", "ws://") + "/game";
        } else {
            // Yerel test için varsayılan URL (backend'inizi yerelde çalıştırıyorsanız)
            this.serverUrl = "ws://localhost:8080/game"; // Port numaranızı kontrol edin
            System.err.println("BASE_URL formatı anlaşılamadı, yerel WebSocket URL'si kullanılıyor: " + this.serverUrl);
        }
        System.out.println("WebSocket URL'si olarak ayarlandı: " + this.serverUrl);
    }

    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void connect() {
        try {
            client = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("WebSocket bağlantısı açıldı! Durum: " + handshakedata.getHttpStatusMessage());
                    // Bağlantı başarılı olduğunda bir test mesajı gönderebilirsiniz
                    // sendMessage(Map.of("type", "CLIENT_GREETING", "content", "Merhaba Sunucu!"));
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Sunucudan mesaj alındı: " + message);
                    try {
                        // Gelen JSON mesajını Map'e çevir
                        Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {});
                        if (onMessageReceived != null) {
                            // Platform.runLater UI thread'inde çalıştırmak için önemli
                            javafx.application.Platform.runLater(() -> onMessageReceived.accept(messageMap));
                        }
                    } catch (Exception e) {
                        System.err.println("Gelen WebSocket mesajı parse edilirken hata: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket bağlantısı kapandı. Kod: " + code + ", Neden: " + reason + ", Uzak sunucu mu: " + remote);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket hatası: " + ex.getMessage());
                    ex.printStackTrace();
                }
            };
            client.connect(); // Bağlantıyı başlat
        } catch (URISyntaxException e) {
            System.err.println("WebSocket URL hatası: " + serverUrl + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Gelen mesajları dinlemek için bir listener set etme metodu
    public void setOnMessageReceived(Consumer<Map<String, Object>> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    // Sunucuya JSON formatında mesaj gönderme metodu
    public void sendMessage(Object messageObject) {
        if (client != null && client.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(messageObject);
                System.out.println("Sunucuya gönderiliyor: " + jsonMessage);
                client.send(jsonMessage);
            } catch (Exception e) {
                System.err.println("WebSocket mesajı gönderilirken hata: " + e.getMessage());
            }
        } else {
            System.err.println("WebSocket bağlantısı açık değil. Mesaj gönderilemedi.");
        }
    }

    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }

    public boolean isOpen() {
        return client != null && client.isOpen();
    }
}