package com.group12.uno.handler;

import com.fasterxml.jackson.databind.ObjectMapper; // JSON işlemleri için
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component // Spring Bean olarak işaretle
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);
    // Aktif WebSocket oturumlarını tutmak için (thread-safe map)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON (de)serialization için

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        logger.info("Yeni WebSocket bağlantısı kuruldu: Session ID - {}, Remote Address - {}", session.getId(), session.getRemoteAddress());

        // Yeni bağlanan kullanıcıya bir hoş geldin mesajı gönderebiliriz
        Map<String, String> welcomeMessage = Map.of(
                "type", "CONNECTION_SUCCESS",
                "sessionId", session.getId(),
                "message", "Sunucuya başarıyla bağlandınız!"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("Mesaj alındı: Session ID - {}, Payload - {}", session.getId(), payload);

        try {
            // Gelen mesajı işle (Örnek: JSON parse et)
            // Map<String, Object> receivedData = objectMapper.readValue(payload, Map.class);
            // String messageType = (String) receivedData.get("type");
            // logger.info("Mesaj tipi: {}", messageType);

            // Şimdilik gelen mesajı tüm bağlı istemcilere (kendisi dahil) geri yolla
            broadcast("Alınan mesaj: " + payload, session.getId());

        } catch (Exception e) {
            logger.error("Mesaj işlenirken hata: ", e);
            Map<String, String> errorMessage = Map.of("type", "ERROR", "message", "Geçersiz mesaj formatı.");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        logger.info("WebSocket bağlantısı kapandı: Session ID - {}, Durum - {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport hatası: Session ID - " + session.getId(), exception);
    }

    // Belirli bir mesajı tüm bağlı client'lara gönderir
    public void broadcast(String messageContent, String senderSessionId) {
        Map<String, String> messageMap = Map.of(
                "type", "BROADCAST_MESSAGE",
                "sender", senderSessionId, // İsteğe bağlı: göndereni belirtmek için
                "content", messageContent
        );

        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageMap)));
                }
            } catch (IOException e) {
                logger.error("Broadcast mesajı gönderilirken hata: Session ID - " + session.getId(), e);
            }
        });
    }

    // Belirli bir client'a mesaj gönderir (henüz kullanılmıyor ama ileride lazım olacak)
    public void sendMessageToSession(String sessionId, Object messageObject) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageObject)));
            } catch (IOException e) {
                logger.error("Belirli bir oturuma mesaj gönderilirken hata: Session ID - " + sessionId, e);
            }
        }
    }
}
