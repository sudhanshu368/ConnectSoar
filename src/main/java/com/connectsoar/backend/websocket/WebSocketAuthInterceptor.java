package com.connectsoar.backend.websocket;

import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.SupabaseAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final SupabaseAuthService supabaseAuthService;

    public WebSocketAuthInterceptor(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isBlank()) {
                String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7).trim();
                }
            }

            if (token != null && !token.isBlank()) {
                try {
                    UserPrincipal principal = supabaseAuthService.verifyToken(token);
                    attributes.put("userPrincipal", principal);
                    attributes.put("userId", principal.getUserId());
                    return true;
                } catch (Exception e) {
                    log.warn("WebSocket handshake token validation failed: {}", e.getMessage());
                }
            }
        }

        log.warn("WebSocket handshake rejected: missing or invalid authentication token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
