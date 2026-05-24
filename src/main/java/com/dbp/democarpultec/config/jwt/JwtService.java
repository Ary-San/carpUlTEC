package com.dbp.democarpultec.config.jwt;

import com.dbp.democarpultec.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

	@Value("${spring.security.jwt.secret:carpultec-development-secret-carpultec-development-secret}")
	private String secret;

	@Value("${spring.security.jwt.expiration:86400000}")
	private long expiration;

	public String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", user.getEmail());
		claims.put("verified", user.isVerified());
		claims.put("roles", user.getRoles());
		return buildToken(claims, user.getId().toString());
	}

	public Long extractUserId(String token) {
		return Long.valueOf(extractClaims(token).getSubject());
	}

	public String extractEmail(String token) {
		return extractClaims(token).get("email", String.class);
	}

	public boolean isTokenValid(String token) {
		return extractUserId(token) != null;
	}

	public boolean isTokenValid(String token, User user) {
		return user.getId().equals(extractUserId(token)) && user.getEmail().equals(extractEmail(token));
	}

	private String buildToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	private Claims extractClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private javax.crypto.SecretKey getSigningKey() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to initialize JWT signing key", exception);
		}
	}
}
