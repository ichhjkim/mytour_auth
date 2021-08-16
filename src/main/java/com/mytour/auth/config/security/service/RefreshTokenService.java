package com.mytour.auth.config.security.service;

import com.mytour.auth.domain.RefreshToken;
import com.mytour.auth.repository.MemberRepo;
import com.mytour.auth.repository.RefreshTokenRepo;
import com.mytour.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.secret.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepo refreshTokenRepository;

    @Autowired
    private MemberRepo userRepository;

    private Logger logger = LoggerFactory.getLogger(RefreshToken.class);

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setMemberDTO(userRepository.findByUsername(username).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        logger.info("######## REFRESHTOKEN CREATE: {}", refreshToken.getToken());
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) throws Exception {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            logger.error("###### REFRESHTOKEN EXPIRATION: {}", token);
            throw new Exception("잘못된 접근");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(String username) {
        return refreshTokenRepository.deleteByMemberDTO(userRepository.findByUsername(username).get());
    }
}
