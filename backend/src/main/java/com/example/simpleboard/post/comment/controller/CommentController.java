package com.example.simpleboard.post.comment.controller;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.common.response.ResponseMessage;
import com.example.simpleboard.post.comment.dto.CommentRequest;
import com.example.simpleboard.post.comment.dto.CommentResponse;
import com.example.simpleboard.post.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ResponseEntity<ResponseMessage<List<CommentResponse>>> getComments(@PathVariable long postId) {
        return ResponseEntity.ok(
                ResponseMessage.data(ResponseCode.REQUEST_SUCCESS, commentService.getComments(postId)));
    }

    @PostMapping
    public ResponseEntity<ResponseMessage<Void>> addComment(@RequestBody @Valid CommentRequest request,
                                                            @AuthenticationPrincipal User loginUser) {
        commentService.addComment(request, loginUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseMessage<Void>> deleteComment(@PathVariable long commentId,
                                                               @AuthenticationPrincipal User loginUser) {
        commentService.deleteComment(commentId, loginUser.getUsername());

        return ResponseEntity.ok(ResponseMessage.of(ResponseCode.REQUEST_SUCCESS));
    }
}
