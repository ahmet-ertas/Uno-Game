package com.group12.uno.config;
import com.group12.uno.handler.GameWebSocketHandler; // Bir sonraki adımda oluşturulacak
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket // WebSocket'i etkinleştirir
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;

    // GameWebSocketHandler'ı enjekte et (Spring DI)
    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/game" endpoint'ini WebSocket bağlantıları için ayarla
        // setAllowedOrigins("*") tüm kaynaklardan gelen bağlantılara izin verir (geliştirme için)
        // Üretimde belirli domain'lere kısıtlamanız daha güvenlidir.
        registry.addHandler(gameWebSocketHandler, "/game")
                .setAllowedOrigins("*");
    }

    // Mesaj boyutu gibi ayarları yapılandırmak için (isteğe bağlı)
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192); // Örnek: Maksimum metin mesajı boyutu
        container.setMaxBinaryMessageBufferSize(8192); // Örnek: Maksimum ikili mesaj boyutu
        return container;
    }
}
