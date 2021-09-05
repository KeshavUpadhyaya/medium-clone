package com.myproject.mediumclone.security;

import com.myproject.mediumclone.exception.TokenRefreshException;
import com.myproject.mediumclone.model.RefreshToken;
import com.myproject.mediumclone.repository.RefreshTokenRepository;
import com.myproject.mediumclone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${myproject.mediumclone.jwt-refresh-token-expiration-ms}")
    private Long refreshTokenDurationMilliseconds;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;


    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(userId.toString())));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMilliseconds));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }


    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(refreshToken.getToken(), "The refresh token is expired. Please make a new login request.");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).orElseThrow());
    }

}
