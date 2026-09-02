package com.example.simpleboard.post.post.dto;

import com.example.simpleboard.post.post.domain.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 게시글 상세. */
@Getter
@Builder
public class PostResponse {

    private long postId;
    private String title;
    private String content;
    private int views;
    private int likes;
    private boolean isPrivate;
    private boolean isBlockComment;
    private String writer;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime postDate;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    public static PostResponse createPostResponse(Post post, List<String> tags) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .views(post.getViews())
                .likes(post.getLikes())
                .isPrivate(post.isPrivate())
                .isBlockComment(post.isBlockComment())
                .writer(post.getWriter().getUserId())
                .postDate(post.getPostDate())
                .tags(tags)
                .build();
    }
}
