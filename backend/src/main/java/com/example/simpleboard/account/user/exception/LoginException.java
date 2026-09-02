package com.example.simpleboard.account.user.exception;

import com.example.simpleboard.common.response.message.AccountMessage;

public class LoginException extends RuntimeException {

    public LoginException(AccountMessage message) {
        super(message.getMessage());
    }

    public LoginException(String message) {
        super(message);
    }
}
