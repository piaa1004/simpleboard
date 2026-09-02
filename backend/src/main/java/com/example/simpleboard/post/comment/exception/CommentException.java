package com.example.simpleboard.post.comment.exception;

import com.example.simpleboard.common.response.message.CommentMessage;

public class CommentException extends RuntimeException {

    public CommentException(CommentMessage message) {
        super(message.getMessage());
    }

    public CommentException(String message) {
        super(message);
    }
}
