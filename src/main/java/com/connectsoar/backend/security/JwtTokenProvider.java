package com.connectsoar.backend.security;

import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.MeetingPermission;
import com.connectsoar.backend.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey signingKey;

    public JwtTokenProvider(@Value("${jwt.secret:ConnectSoarSuperSecretProductionGradeJwtKey2026!WithHighEntropy}") String jwtSecret) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            this.signingKey = Keys.hmacShaKeyFor(padded);
        } else {
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * Generates a restricted, short-lived password reset token (15 mins).
     * Cannot be used for normal application access or meeting join.
     */
    public String generatePasswordResetToken(String userId, String email) {
        long expirationMs = 15 * 60 * 1000L; // 15 minutes
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("token_type", "password_reset")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates a short-lived meeting authorization token (15 mins / 900 seconds).
     */
    public String generateMeetingToken(String userId, String email, String meetingId, String roomId, MeetingPermission permission) {
        long expirationMs = 15 * 60 * 1000L; // 900 seconds
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("meeting_id", meetingId)
                .claim("room_id", roomId)
                .claim("permission", permission.name())
                .claim("token_type", "meeting_join")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates and parses claims from our internally signed token.
     */
    public Claims parseAndValidateClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "Access token has expired. Please refresh your token.", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Invalid JWT signature/format: {}", e.getMessage());
            throw new ApiException(ErrorCode.TOKEN_INVALID, "Invalid or malformed access token.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Validates that the token is specifically a password_reset token.
     */
    public Claims validatePasswordResetToken(String token) {
        Claims claims = parseAndValidateClaims(token);
        String tokenType = claims.get("token_type", String.class);
        if (!"password_reset".equals(tokenType)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Token is not authorized for password reset.", HttpStatus.FORBIDDEN);
        }
        return claims;
    }
}
