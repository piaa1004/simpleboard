package com.example.simpleboard;

import com.example.simpleboard.smtp.repository.MailCertRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원가입 → 로그인 → 글쓰기 → 댓글 → 로그아웃 까지 한 번에 훑는다.
 * 이 프로젝트가 제공하는 기능이 이게 전부라서, 이 테스트가 곧 전체 회귀 테스트다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class BoardFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailCertRepository mailCertRepository;

    @Test
    @DisplayName("게시판 전체 흐름")
    void boardFlow() throws Exception {
        // 회원가입은 이메일 인증을 마쳐야 통과한다.
        verifyEmail("tester@example.com");

        mockMvc.perform(post("/registers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"tester","email":"tester@example.com",
                                 "password":"secret1","checkPassword":"secret1"}
                                """))
                .andExpect(status().isOk());

        // 같은 아이디로는 다시 가입할 수 없다.
        mockMvc.perform(get("/users/exists").param("id", "tester").param("email", "other@example.com"))
                .andExpect(status().isConflict());

        // 쓰이지 않은 값이면 통과한다.
        mockMvc.perform(get("/users/exists").param("id", "another").param("email", "another@example.com"))
                .andExpect(status().isOk());

        // 로그인 전에는 내 정보를 볼 수 없다.
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());

        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/logins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"tester","password":"secret1"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        assertThat(cookies).extracting(Cookie::getName).contains("accessToken", "refreshToken");

        // 틀린 비밀번호는 거절된다.
        mockMvc.perform(post("/logins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"tester","password":"wrong-one"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users").cookie(cookies)).andExpect(status().isOk());

        // 글쓰기는 로그인이 필요하다.
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"제목","content":"내용","tags":["공지"]}
                                """))
                .andExpect(status().isUnauthorized());

        MvcResult created = mockMvc.perform(post("/posts")
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"제목","content":"내용","tags":["공지"],
                                 "privatePost":false,"blockComment":false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long postId = body(created).get("data").asLong();

        // 목록 · 검색 · 태그 조회
        assertThat(dataSize("/posts?page=0&size=10")).isEqualTo(1);
        assertThat(dataSize("/posts?type=search&data=제목&page=0&size=10")).isEqualTo(1);
        assertThat(dataSize("/posts?type=tag&data=공지&page=0&size=10")).isEqualTo(1);
        assertThat(dataSize("/posts?type=search&data=없는말&page=0&size=10")).isZero();

        MvcResult count = mockMvc.perform(get("/posts/count")).andExpect(status().isOk()).andReturn();
        assertThat(body(count).get("data").asLong()).isEqualTo(1);

        // 조회수는 비로그인도 올릴 수 있다.
        mockMvc.perform(patch("/posts/views/" + postId)).andExpect(status().isOk());

        MvcResult detail = mockMvc.perform(get("/posts/" + postId)).andExpect(status().isOk()).andReturn();
        JsonNode data = body(detail).get("data");
        assertThat(data.get("title").asText()).isEqualTo("제목");
        assertThat(data.get("views").asInt()).isEqualTo(1);
        assertThat(data.get("tags").get(0).asText()).isEqualTo("공지");

        // 자기 글은 추천할 수 없다.
        mockMvc.perform(patch("/posts/likes/" + postId).cookie(cookies)).andExpect(status().isBadRequest());

        // 댓글
        mockMvc.perform(post("/comments")
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":%d,"content":"첫 댓글"}
                                """.formatted(postId)))
                .andExpect(status().isCreated());

        assertThat(dataSize("/comments/" + postId)).isEqualTo(1);

        // 수정
        mockMvc.perform(put("/posts/" + postId)
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"고친 제목","content":"고친 내용","tags":[]}
                                """))
                .andExpect(status().isOk());

        MvcResult updated = mockMvc.perform(get("/posts/" + postId)).andExpect(status().isOk()).andReturn();
        assertThat(body(updated).get("data").get("title").asText()).isEqualTo("고친 제목");

        // 삭제하면 목록에서도 상세에서도 사라진다.
        mockMvc.perform(delete("/posts/" + postId).cookie(cookies)).andExpect(status().isOk());
        mockMvc.perform(get("/posts/" + postId)).andExpect(status().isBadRequest());
        assertThat(dataSize("/posts?page=0&size=10")).isZero();

        mockMvc.perform(post("/user/logout").cookie(cookies)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("망가진 JSON 은 400 으로 돌려준다")
    void malformedJsonIsBadRequest() throws Exception {
        mockMvc.perform(post("/logins").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    /** 인증번호를 요청하고, 저장된 코드를 그대로 읽어 인증까지 끝낸다. */
    private void verifyEmail(String email) throws Exception {
        mockMvc.perform(post("/mail/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        String code = mailCertRepository.findByEmail(email).orElseThrow().getVerificationCode();

        mockMvc.perform(post("/mail/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk());
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private int dataSize(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();

        return body(result).get("data").size();
    }
}
