package com.example.simpleboard.post.comment.service;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.account.user.service.LoginService;
import com.example.simpleboard.common.response.message.CommentMessage;
import com.example.simpleboard.post.comment.domain.Comment;
import com.example.simpleboard.post.comment.dto.CommentRequest;
import com.example.simpleboard.post.comment.dto.CommentResponse;
import com.example.simpleboard.post.comment.exception.CommentException;
import com.example.simpleboard.post.comment.repository.CommentRepository;
import com.example.simpleboard.post.post.domain.Post;
import com.example.simpleboard.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final LoginService loginService;

    public List<CommentResponse> getComments(long postId) {
        return commentRepository.findCommentByPostId(postId);
    }

    @Transactional
    public void addComment(CommentRequest request, String userId) {
        Post post = postService.findAlivePost(request.getPostId());

        if (post.isBlockComment()) {
            throw new CommentException(CommentMessage.BLOCKED_COMMENT);
        }

        User user = loginService.findUserByUserId(userId);
        post.addComment(Comment.createComment(request.getContent(), user));
    }

    @Transactional
    public void deleteComment(long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException("댓글을 찾을 수 없습니다."));

        if (!comment.isWrittenBy(userId)) {
            throw new CommentException(CommentMessage.ONLY_OWNER_CAN_DELETE);
        }

        comment.deleteComment();
    }
}
