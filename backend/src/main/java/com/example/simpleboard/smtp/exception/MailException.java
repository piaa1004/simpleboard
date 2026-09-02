package com.example.simpleboard.smtp.exception;

import com.example.simpleboard.common.response.message.MailMessage;

public class MailException extends RuntimeException {

    public MailException(MailMessage message) {
        super(message.getMessage());
    }

    public MailException(MailMessage message, Throwable cause) {
        super(message.getMessage(), cause);
    }
}
