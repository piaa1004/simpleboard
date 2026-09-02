package com.example.simpleboard.post.comment.repository;

import com.example.simpleboard.post.comment.dto.CommentResponse;

import java.util.List;

public interface CustomCommentRepository {

    List<CommentResponse> findCommentByPostId(long postId);
}
