package com.mytour.auth.repository;

import com.mytour.auth.domain.MemberAgreeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberAgreeRepo extends JpaRepository<MemberAgreeDTO, Long> {
}
