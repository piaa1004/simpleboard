package com.example.simpleboard.smtp.controller;

import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.common.response.ResponseMessage;
import com.example.simpleboard.smtp.dto.MailCheckRequest;
import com.example.simpleboard.smtp.dto.MailSendRequest;
import com.example.simpleboard.smtp.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    /** 인증번호 발송. 이미 가입된 이메일이면 409. */
    @PostMapping("/send")
    public ResponseEntity<ResponseMessage<Void>> sendMail(@RequestBody @Valid MailSendRequest request) {
        mailService.sendVerificationCode(request.getEmail());

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS, "인증번호를 보냈습니다."));
    }

    /** 인증번호 확인. 성공하면 그 이메일로 회원가입할 수 있게 된다. */
    @PostMapping("/check")
    public ResponseEntity<ResponseMessage<Void>> checkMail(@RequestBody @Valid MailCheckRequest request) {
        mailService.checkVerificationCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS, "이메일 인증이 완료되었습니다."));
    }
}
