package com.laundry.security;

import com.laundry.entity.User;
import com.laundry.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

/**
 * Manages JWT token creation/validation,
 * plus some convenience methods for extracting the current user from Authentication.
 */
@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;  // Base64 Encoded

    private SecretKey signInKey;

    private final UserRepository userRepository;

    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("Signing Key Initialized.");
    }

    private SecretKey getSignInKey() {
        return signInKey;
    }

    // --- Basic token claims extraction ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenNotExpired(String token) {
        return extractExpiration(token).after(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isTokenNotExpired(token));
    }

    // --- Creation ---

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            claims.put("userId", user.getId());
            // If role is "ADMIN", store "ROLE_ADMIN" in claims, etc.
            claims.put("role", "ROLE_" + user.getRole());
        }
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (1000 * 60 * 60 * 24)); // 24 hours

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- Additional convenience methods ---

    /**
     * If your Authentication principal is a custom user details containing userId,
     * or if your JWT is included in a Bearer token and you've put userId in the claims,
     * you can retrieve it here.
     */
    public static Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LaundryUserDetails)) {
            return null;
        }
        LaundryUserDetails principal = (LaundryUserDetails) authentication.getPrincipal();
        return principal.getId();
    }

    public static String getRoleFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }
}
