package com.mytour.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.Instant;

@Entity(name = "refresh_token")
@Getter
@Setter
@ToString
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long refreshTokenId;

    @OneToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    private MemberDTO memberDTO;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}