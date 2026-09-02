package com.example.simpleboard.post.post.controller;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.common.response.ResponseMessage;
import com.example.simpleboard.post.post.dto.PostListResponse;
import com.example.simpleboard.post.post.dto.PostRequest;
import com.example.simpleboard.post.post.dto.PostResponse;
import com.example.simpleboard.post.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * 목록 · 검색 · 태그 조회를 한 엔드포인트로 받는다.
     * type = normal | search | tag, data = 검색어.
     */
    @GetMapping
    public ResponseEntity<ResponseMessage<List<PostListResponse>>> getPostList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String data,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.data(ResponseCode.REQUEST_SUCCESS,
                postService.getPostList(type, data, pageable)));
    }

    /** 페이지네이션에 필요한 전체 건수. */
    @GetMapping("/count")
    public ResponseEntity<ResponseMessage<Long>> getTotalNumberOfPosts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String data) {
        return ResponseEntity.ok(ResponseMessage.data(ResponseCode.REQUEST_SUCCESS,
                postService.getTotalNumberOfPosts(type, data)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ResponseMessage<PostResponse>> findPost(@PathVariable long postId,
                                                                  @AuthenticationPrincipal User loginUser) {
        String userId = loginUser == null ? null : loginUser.getUsername();

        return ResponseEntity.ok(ResponseMessage.data(ResponseCode.REQUEST_SUCCESS,
                postService.findPostByPostId(postId, userId)));
    }

    @PostMapping
    public ResponseEntity<ResponseMessage<Long>> addPost(@RequestBody @Valid PostRequest postRequest,
                                                          @AuthenticationPrincipal User loginUser) {
        long postId = postService.addPost(postRequest, loginUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseMessage.data(ResponseCode.REQUEST_SUCCESS, postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ResponseMessage<Void>> updatePost(@PathVariable long postId,
                                                            @RequestBody @Valid PostRequest postRequest,
                                                            @AuthenticationPrincipal User loginUser) {
        postService.updatePost(postId, postRequest, loginUser.getUsername());

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseMessage<Void>> deletePost(@PathVariable long postId,
                                                            @AuthenticationPrincipal User loginUser) {
        postService.deletePost(postId, loginUser.getUsername());

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }

    /** 조회수 증가. 비로그인도 가능하다. */
    @PatchMapping("/views/{postId}")
    public ResponseEntity<ResponseMessage<Void>> increasePostView(@PathVariable long postId) {
        postService.increasePostView(postId);

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }

    @PatchMapping("/likes/{postId}")
    public ResponseEntity<ResponseMessage<Void>> increasePostLike(@PathVariable long postId,
                                                                  @AuthenticationPrincipal User loginUser) {
        postService.increasePostLike(postId, loginUser.getUsername());

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }
}
