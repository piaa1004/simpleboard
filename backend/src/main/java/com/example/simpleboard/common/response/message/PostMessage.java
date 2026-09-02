package com.example.simpleboard.common.response.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostMessage {
    NOT_FOUND_POST("게시글을 찾을 수 없습니다."),
    ALREADY_RECOMMENDED("이미 추천한 게시글입니다."),
    CANNOT_RECOMMEND_OWN_POST("자신의 글은 추천할 수 없습니다."),
    ONLY_OWNER_CAN_DELETE("본인이 작성한 게시글만 삭제할 수 있습니다."),
    ONLY_OWNER_CAN_MODIFY("본인이 작성한 게시글만 수정할 수 있습니다."),
    IS_DELETE_POST("삭제된 게시글입니다."),
    IS_PRIVATE_POST("비공개 게시글입니다.");

    private final String message;
}
