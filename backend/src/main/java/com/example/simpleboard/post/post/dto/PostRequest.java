package com.example.simpleboard.post.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 50, message = "제목은 50자를 넘을 수 없습니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 1100, message = "내용이 너무 깁니다.")
    private String content;

    /** 최대 5개. 빈 값은 저장 단계에서 걸러진다. */
    private String[] tags;

    private boolean privatePost;

    private boolean blockComment;
}
