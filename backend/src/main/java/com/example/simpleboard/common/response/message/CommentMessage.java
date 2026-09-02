package com.example.simpleboard.common.response.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentMessage {
    ONLY_OWNER_CAN_DELETE("댓글 작성자만 삭제할 수 있습니다."),
    BLOCKED_COMMENT("댓글이 차단된 게시글입니다.");

    private final String message;
}
