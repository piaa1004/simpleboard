package com.example.simpleboard.account.user.controller;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.account.user.dto.LoginRequest;
import com.example.simpleboard.account.user.dto.RegisterRequest;
import com.example.simpleboard.account.user.dto.UserResponse;
import com.example.simpleboard.account.user.service.LoginService;
import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.common.response.ResponseMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /** 회원가입. */
    @PostMapping("/registers")
    public ResponseEntity<ResponseMessage<Void>> register(@RequestBody @Valid RegisterRequest request) {
        loginService.register(request);

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS, "회원가입이 완료되었습니다."));
    }

    /** 아이디·이메일 중복 확인. 사용 가능하면 200, 이미 있으면 401 과 사유를 돌려준다. */
    @GetMapping("/users/exists")
    public ResponseEntity<ResponseMessage<Void>> checkAvailable(@RequestParam String id,
                                                                @RequestParam String email) {
        loginService.checkAvailable(id, email);

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }

    /** 로그인. 액세스·리프레시 토큰을 HttpOnly 쿠키로 내려준다. */
    @PostMapping("/logins")
    public ResponseEntity<ResponseMessage<Void>> login(@RequestBody @Valid LoginRequest request,
                                                       HttpServletResponse response) {
        loginService.login(request, response);

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.LOGIN_SUCCESS));
    }

    /** 로그아웃. 저장된 리프레시 토큰을 지우고 쿠키를 만료시킨다. */
    @PostMapping("/user/logout")
    public ResponseEntity<ResponseMessage<Void>> logout(@AuthenticationPrincipal User loginUser,
                                                        HttpServletResponse response) {
        loginService.logout(loginUser == null ? null : loginUser.getUsername(), response);

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.LOGOUT_SUCCESS));
    }

    /** 현재 로그인한 사용자. 이 요청이 401 이면 프론트엔드는 비로그인으로 판단한다. */
    @GetMapping("/users")
    public ResponseEntity<ResponseMessage<UserResponse>> findMe(@AuthenticationPrincipal User loginUser) {
        return ResponseEntity.ok(
                ResponseMessage.data(ResponseCode.REQUEST_SUCCESS, loginService.findUser(loginUser.getUsername())));
    }
}
