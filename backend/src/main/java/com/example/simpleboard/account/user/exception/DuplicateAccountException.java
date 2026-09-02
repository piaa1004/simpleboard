package com.example.simpleboard.account.user.exception;

import com.example.simpleboard.common.response.message.AccountMessage;

/**
 * 이미 쓰이고 있는 아이디·이메일로 가입하려 한 경우.
 * 로그인 실패(401)와 달리 "인증에 실패한 것"이 아니라 "값이 겹친 것"이라 409 로 내려간다.
 */
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(AccountMessage message) {
        super(message.getMessage());
    }
}
