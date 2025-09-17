package com.denos.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;
    private final String issuer;

    public JwtUtil(@Value("${security.jwt.secret}") String secret,
                   @Value("${security.jwt.expiration-ms}") long expirationMs,
                   @Value("${security.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String getUsername(String token) {
        return validateToken(token).getBody().getSubject();
    }
}