package com.example.simpleboard.post.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotNull(message = "게시글 번호가 없습니다.")
    private Long postId;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 200, message = "댓글은 200자를 넘을 수 없습니다.")
    private String content;
}
