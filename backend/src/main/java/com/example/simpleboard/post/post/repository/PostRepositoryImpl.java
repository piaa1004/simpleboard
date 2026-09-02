package com.example.simpleboard.post.post.repository;

import com.example.simpleboard.post.post.domain.Post;
import com.example.simpleboard.post.post.dto.PostListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.example.simpleboard.account.user.domain.QUser.user;
import static com.example.simpleboard.post.comment.domain.QComment.comment;
import static com.example.simpleboard.post.post.domain.QPost.post;
import static com.example.simpleboard.post.tag.domain.QTag.tag;

@RequiredArgsConstructor
public class PostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostListResponse> getPostList(Pageable pageable) {
        return listQuery(pageable, null, null);
    }

    @Override
    public List<PostListResponse> findPostBySearch(Pageable pageable, String keyword) {
        return listQuery(pageable, keyword, null);
    }

    @Override
    public List<PostListResponse> findPostByTag(Pageable pageable, String tagData) {
        return listQuery(pageable, null, tagData);
    }

    @Override
    public long getTotalNumberOfPosts() {
        return countOrZero(queryFactory.select(post.count())
                .from(post)
                .where(post.isDelete.eq(false))
                .fetchOne());
    }

    @Override
    public long getTotalNumberOfSearchPosts(String keyword) {
        return countOrZero(queryFactory.select(post.count())
                .from(post)
                .where(post.isDelete.eq(false)
                        .and(post.title.contains(keyword).or(post.content.contains(keyword))))
                .fetchOne());
    }

    @Override
    public long getTotalNumberOfTagSearchPosts(String tagData) {
        return countOrZero(queryFactory.select(post.countDistinct())
                .from(post)
                .join(post.tags, tag)
                .where(post.isDelete.eq(false).and(tag.tagData.eq(tagData)))
                .fetchOne());
    }

    @Override
    public Optional<Post> findPostWithWriter(long postId) {
        return Optional.ofNullable(queryFactory.selectFrom(post)
                .join(post.writer, user).fetchJoin()
                .where(post.postId.eq(postId))
                .fetchOne());
    }

    @Override
    public List<String> findTagsInPostId(long postId) {
        return queryFactory.select(tag.tagData)
                .from(post)
                .join(post.tags, tag)
                .where(post.postId.eq(postId))
                .fetch();
    }

    @Override
    public void increasePostView(long postId) {
        queryFactory.update(post)
                .set(post.views, post.views.add(1))
                .where(post.postId.eq(postId))
                .execute();
    }

    /**
     * 목록·검색·태그 조회는 결과 모양이 같고 조건만 다르다.
     * 댓글 수를 함께 세야 해서 left join + groupBy 가 필요하다.
     */
    private List<PostListResponse> listQuery(Pageable pageable, String keyword, String tagData) {
        var query = queryFactory
                .select(Projections.constructor(PostListResponse.class,
                        post.postId, post.title, user.userId, post.postDate,
                        post.likes, post.views, comment.count()))
                .from(post)
                .join(post.writer, user)
                .leftJoin(post.comments, comment).on(comment.isDelete.eq(false));

        if (tagData != null) {
            query.join(post.tags, tag).on(tag.tagData.eq(tagData));
        }

        if (keyword != null) {
            query.where(post.title.contains(keyword).or(post.content.contains(keyword)));
        }

        return query
                .where(post.isDelete.eq(false))
                // 집계하지 않는 컬럼을 모두 나열한다. MySQL 의 ONLY_FULL_GROUP_BY 와 H2 둘 다 이 형태를 요구한다.
                .groupBy(post.postId, post.title, user.userId, post.postDate, post.likes, post.views)
                .orderBy(post.postId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private long countOrZero(Long count) {
        return count == null ? 0L : count;
    }
}
