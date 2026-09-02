package com.example.simpleboard.post.post.repository;

import com.example.simpleboard.post.post.domain.Post;
import com.example.simpleboard.post.post.dto.PostListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** 목록 조회처럼 JPQL 로 쓰기 번거로운 질의를 QueryDSL 로 구현한다. */
public interface CustomPostRepository {

    List<PostListResponse> getPostList(Pageable pageable);

    List<PostListResponse> findPostBySearch(Pageable pageable, String keyword);

    List<PostListResponse> findPostByTag(Pageable pageable, String tagData);

    long getTotalNumberOfPosts();

    long getTotalNumberOfSearchPosts(String keyword);

    long getTotalNumberOfTagSearchPosts(String tagData);

    /** 작성자까지 함께 읽어 온다. 상세 화면이 항상 작성자를 필요로 하기 때문이다. */
    Optional<Post> findPostWithWriter(long postId);

    List<String> findTagsInPostId(long postId);

    void increasePostView(long postId);
}
