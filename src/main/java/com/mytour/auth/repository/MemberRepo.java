package com.mytour.auth.repository;

import com.mytour.auth.domain.MemberDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepo extends JpaRepository<MemberDTO, String> {

    Optional<MemberDTO> findByUsername(String username);
    Optional<MemberDTO> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
