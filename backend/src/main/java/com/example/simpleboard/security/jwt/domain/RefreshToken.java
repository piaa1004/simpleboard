package com.example.simpleboard.security.jwt.domain;

import com.example.simpleboard.security.jwt.dto.Token;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(nullable = false, length = 512)
    private String token;

    /** 토큰의 주인(사용자 아이디). 한 사용자당 한 행만 남긴다. */
    @Column(nullable = false)
    private String keyId;

    public static RefreshToken createRefreshToken(Token token) {
        return RefreshToken.builder()
                .keyId(token.getKey())
                .token(token.getRefreshToken())
                .build();
    }
}
