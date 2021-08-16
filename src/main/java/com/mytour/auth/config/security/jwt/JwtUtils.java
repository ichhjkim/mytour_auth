package com.mytour.auth.config.security.jwt;

import com.mytour.auth.config.security.service.UserDetailsImpl;
import com.mytour.auth.domain.RefreshToken;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    @Value("${jwt.secret.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("##### JWTTOKEN ERROR: {}", e.toString());
        } catch (MalformedJwtException e) {
            logger.error("##### JWTTOKEN ERROR: {}", e.toString());
        } catch (ExpiredJwtException e) {
            logger.error("##### JWTTOKEN ERROR: {}", e.toString());
        } catch (UnsupportedJwtException e) {
            logger.error("##### JWTTOKEN ERROR: {}", e.toString());
        } catch (IllegalArgumentException e) {
            logger.error("##### JWTTOKEN ERROR: {}", e.toString());
        }
        return false;
    }
}
