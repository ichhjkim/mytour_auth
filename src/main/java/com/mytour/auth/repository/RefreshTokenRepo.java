package com.mytour.auth.repository;

import com.mytour.auth.domain.MemberDTO;
import com.mytour.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByMemberDTO(MemberDTO memberDTO);
}
