package com.example.simpleboard.account.user.dto;

import com.example.simpleboard.account.user.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 화면에 내려보내는 회원 정보. 비밀번호는 절대 담지 않는다. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long userKey;

    /** 로그인 아이디. 프론트엔드에서는 이 값을 사용자 표시명으로 쓴다. */
    private String id;

    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate joinDate;

    public static UserResponse createResponse(User user) {
        return UserResponse.builder()
                .userKey(user.getUserKey())
                .id(user.getUserId())
                .email(user.getEmail())
                .joinDate(user.getJoinDate())
                .build();
    }
}
