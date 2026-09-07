package com.connectsoar.backend.config;

import com.connectsoar.backend.websocket.MeetingSignalingHandler;
import com.connectsoar.backend.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MeetingSignalingHandler meetingSignalingHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(MeetingSignalingHandler meetingSignalingHandler,
                           WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.meetingSignalingHandler = meetingSignalingHandler;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meetingSignalingHandler, "/ws/meetings/{meetingId}", "/ws/meetings/**")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }
}
