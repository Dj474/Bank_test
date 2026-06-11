package com.example.bankcards.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.access-token.secret}")
    private String jwtAccessSecret;

    @Value("${jwt.access-token.expiration}")
    private long jwtAccessExpiration;

    @Value("${jwt.refresh-token.secret}")
    private String jwtRefreshSecret;

    @Value("${jwt.refresh-token.expiration}")
    private long jwtRefreshExpiration;

    private Key getAccessTokenSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtAccessSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Key getRefreshTokenSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtRefreshSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessTokenFromId(Long id, String role) {
        Claims claims = Jwts.claims().setSubject(id.toString());
        claims.put("role", "ROLE_" + role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessExpiration))
                .signWith(getAccessTokenSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshTokenFromId(Long id) {
        return Jwts.builder()
                .setSubject(id.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(getRefreshTokenSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromAccessToken(String token) {
        String stringId = Jwts.parserBuilder()
                .setSigningKey(getAccessTokenSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();

        return Long.parseLong(stringId);
    }

    public String getRoleFromAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getAccessTokenSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public Long getUserIdFromRefreshToken(String token) {
        String stringId = Jwts.parserBuilder()
                .setSigningKey(getRefreshTokenSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();

        return Long.parseLong(stringId);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, getAccessTokenSigningKey());
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, getRefreshTokenSigningKey());
    }

    private boolean validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}