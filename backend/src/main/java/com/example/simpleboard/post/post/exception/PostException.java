package com.example.simpleboard.post.post.exception;

import com.example.simpleboard.common.response.message.PostMessage;

public class PostException extends RuntimeException {

    public PostException(PostMessage message) {
        super(message.getMessage());
    }

    public PostException(String message) {
        super(message);
    }
}
