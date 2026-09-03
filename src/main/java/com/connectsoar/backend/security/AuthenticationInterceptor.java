package com.connectsoar.backend.security;

import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.service.SupabaseAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private final SupabaseAuthService supabaseAuthService;

    public AuthenticationInterceptor(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Check for public endpoint annotation
        if (handler instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class) ||
                handlerMethod.getBeanType().isAnnotationPresent(PublicEndpoint.class)) {
                return true;
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Missing or invalid Authorization header. Expected 'Authorization: Bearer <token>'", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Bearer token cannot be empty.", HttpStatus.UNAUTHORIZED);
        }

        UserPrincipal principal = supabaseAuthService.verifyToken(token);
        request.setAttribute("userPrincipal", principal);
        request.setAttribute("currentUser", principal);

        // Enforce reset_password gatekeeping: Users with reset_password=true cannot access protected application APIs
        String requestUri = request.getRequestURI();
        if (principal.isResetPassword() && !requestUri.endsWith("/change-password") && !requestUri.endsWith("/logout")) {
            log.warn("Access blocked for user {}: reset_password is required", principal.getUserId());
            throw new ApiException(ErrorCode.PASSWORD_CHANGE_REQUIRED, "Password change is required before accessing the application.", HttpStatus.FORBIDDEN);
        }

        // Check role authorization
        if (handler instanceof HandlerMethod handlerMethod) {
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }

            if (requireRole != null) {
                Role[] allowedRoles = requireRole.value();
                boolean hasRole = Arrays.asList(allowedRoles).contains(principal.getRole());
                if (!hasRole) {
                    log.warn("Access denied for user {}: requires role {} but user has {}", 
                            principal.getUserId(), Arrays.toString(allowedRoles), principal.getRole());
                    throw new ApiException(ErrorCode.FORBIDDEN, "Access denied: insufficient permissions.", HttpStatus.FORBIDDEN);
                }
            }
        }

        return true;
    }
}
