package com.example.simpleboard.post.comment.repository;

import com.example.simpleboard.post.comment.dto.CommentResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.simpleboard.account.user.domain.QUser.user;
import static com.example.simpleboard.post.comment.domain.QComment.comment;
import static com.example.simpleboard.post.post.domain.QPost.post;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CustomCommentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentResponse> findCommentByPostId(long postId) {
        return queryFactory
                .select(Projections.constructor(CommentResponse.class,
                        post.postId, comment.commentId, comment.content, comment.postDate, user.userId))
                .from(comment)
                .join(comment.writer, user)
                .join(comment.post, post)
                .where(post.postId.eq(postId).and(comment.isDelete.eq(false)))
                .orderBy(comment.commentId.asc())
                .fetch();
    }
}
