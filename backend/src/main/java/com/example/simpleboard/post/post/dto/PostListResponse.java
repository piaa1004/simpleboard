package com.example.simpleboard.post.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 목록 한 줄. QueryDSL 이 생성자로 채운다. 필드 순서를 바꾸면 조회 쿼리도 같이 고쳐야 한다. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

    /** 게시글 번호. */
    private long numbers;
    private String title;
    private String writer;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime postDate;

    private int likes;
    private int views;

    /** 댓글 수. */
    private long count;
}
