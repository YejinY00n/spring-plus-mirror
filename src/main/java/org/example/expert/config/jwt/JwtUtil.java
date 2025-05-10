package org.example.expert.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.jwt.dto.JwtToken;
import org.example.expert.config.security.CustomUserDetails;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // Access, Refresh 토큰 사용
    public JwtToken createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Date date = new Date();
        long now = date.getTime();

        String accessToken = Jwts.builder()
            .setSubject(authentication.getName())
                        .claim("auth", authorities)
                        .claim("email", customUserDetails.getUsername())
                        .claim("nickname", customUserDetails.getNickname())
                        .setExpiration(new Date(now + TOKEN_TIME))
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();

        String refreshToken = Jwts.builder()
            .setExpiration(new Date(now + 86400000))
            .signWith(key, signatureAlgorithm) // 암호화 알고리즘
            .compact();

        return new JwtToken("Bearer", accessToken, refreshToken);

//        return BEARER_PREFIX +
//                Jwts.builder()
//                        .setSubject(String.valueOf(userId))
//                        .claim("email", email)
//                        .claim("userRole", userRole)
//                        .claim("nickname", nickname)            // Lv2. claim 에 nickname 정보 추가
//                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
//                        .setIssuedAt(date) // 발급일
//                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
//                        .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new ServerException("Not Found Token");
    }

//    public Claims extractClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
        } catch (Exception e) {
            log.error("Internal server error", e);
        }

        return null;
    }
}
