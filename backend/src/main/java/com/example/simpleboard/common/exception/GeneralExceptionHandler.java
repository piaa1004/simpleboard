package com.example.simpleboard.common.exception;

import com.example.simpleboard.account.user.exception.DuplicateAccountException;
import com.example.simpleboard.account.user.exception.LoginException;
import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.common.response.ResponseMessage;
import com.example.simpleboard.post.comment.exception.CommentException;
import com.example.simpleboard.post.post.exception.PostException;
import com.example.simpleboard.security.jwt.exception.InvalidTokenException;
import com.example.simpleboard.smtp.exception.MailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** 컨트롤러·서비스에서 던진 예외를 공통 응답 봉투로 바꾼다. */
@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ResponseMessage<Void>> handleLogin(LoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseMessage.of(ResponseCode.REQUEST_FAIL, e.getMessage()));
    }

    /** 아이디·이메일 중복은 인증 실패가 아니라 값의 충돌이다. */
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ResponseMessage<Void>> handleDuplicateAccount(DuplicateAccountException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseMessage.of(ResponseCode.REQUEST_FAIL, e.getMessage()));
    }

    @ExceptionHandler({PostException.class, CommentException.class, MailException.class})
    public ResponseEntity<ResponseMessage<Void>> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(ResponseMessage.of(ResponseCode.REQUEST_FAIL, e.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ResponseMessage<Void>> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage.of(ResponseCode.INVALID_TOKEN, e.getMessage()));
    }

    /**
     * 본문이 JSON 으로 읽히지 않는 경우.
     * 이걸 잡지 않으면 스프링 기본 /error 응답이 나가서 프론트가 기대하는 봉투 모양이 깨진다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseMessage<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ResponseMessage.of(ResponseCode.REQUEST_FAIL, "요청 본문을 읽을 수 없습니다."));
    }

    /** @Valid 검증 실패. 어느 필드가 왜 틀렸는지 한 줄로 모아 돌려준다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMessage<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("\n"));

        return ResponseEntity.badRequest()
                .body(ResponseMessage.of(ResponseCode.REQUEST_FAIL, message));
    }
}
