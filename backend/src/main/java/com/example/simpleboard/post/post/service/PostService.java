package com.example.simpleboard.post.post.service;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.account.user.service.LoginService;
import com.example.simpleboard.common.response.message.PostMessage;
import com.example.simpleboard.post.post.domain.Post;
import com.example.simpleboard.post.post.dto.PostListResponse;
import com.example.simpleboard.post.post.dto.PostRequest;
import com.example.simpleboard.post.post.dto.PostResponse;
import com.example.simpleboard.post.post.exception.PostException;
import com.example.simpleboard.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    /** 태그는 화면과 같은 개수로 제한한다. 서버만 믿고 저장하면 안 되기 때문이다. */
    private static final int MAX_TAGS = 5;

    private final PostRepository postRepository;
    private final LoginService loginService;

    @Transactional
    public long addPost(PostRequest postRequest, String userId) {
        validateTags(postRequest);

        User user = loginService.findUserByUserId(userId);
        Post post = Post.createPost(postRequest);

        user.addPost(post);

        return postRepository.save(post).getPostId();
    }

    @Transactional
    public void updatePost(long postId, PostRequest postRequest, String userId) {
        validateTags(postRequest);

        Post post = findAlivePost(postId);
        if (!post.isWrittenBy(userId)) {
            throw new PostException(PostMessage.ONLY_OWNER_CAN_MODIFY);
        }

        post.updatePost(postRequest);
    }

    @Transactional
    public void deletePost(long postId, String userId) {
        Post post = findAlivePost(postId);
        if (!post.isWrittenBy(userId)) {
            throw new PostException(PostMessage.ONLY_OWNER_CAN_DELETE);
        }

        post.deletePost();
    }

    /**
     * 추천. 자기 글은 추천할 수 없고, 같은 사람이 두 번 추천할 수도 없다.
     */
    @Transactional
    public void increasePostLike(long postId, String userId) {
        Post post = findAlivePost(postId);

        if (post.isWrittenBy(userId)) {
            throw new PostException(PostMessage.CANNOT_RECOMMEND_OWN_POST);
        }

        User user = loginService.findUserByUserId(userId);
        if (post.isRecommendedBy(user)) {
            throw new PostException(PostMessage.ALREADY_RECOMMENDED);
        }

        post.addRecommendation(user);
    }

    /** 조회수. 본문을 읽어오지 않고 UPDATE 한 번으로 끝낸다. */
    @Transactional
    public void increasePostView(long postId) {
        postRepository.increasePostView(postId);
    }

    public List<PostListResponse> getPostList(String type, String keyword, Pageable pageable) {
        return switch (nullToNormal(type)) {
            case "search" -> postRepository.findPostBySearch(pageable, keyword);
            case "tag" -> postRepository.findPostByTag(pageable, keyword);
            default -> postRepository.getPostList(pageable);
        };
    }

    public long getTotalNumberOfPosts(String type, String keyword) {
        return switch (nullToNormal(type)) {
            case "search" -> postRepository.getTotalNumberOfSearchPosts(keyword);
            case "tag" -> postRepository.getTotalNumberOfTagSearchPosts(keyword);
            default -> postRepository.getTotalNumberOfPosts();
        };
    }

    /** 상세 조회. 비공개 글은 작성자만 볼 수 있다. */
    public PostResponse findPostByPostId(long postId, String userId) {
        Post post = findAlivePost(postId);

        if (post.isPrivate() && !post.isWrittenBy(userId)) {
            throw new PostException(PostMessage.IS_PRIVATE_POST);
        }

        return PostResponse.createPostResponse(post, postRepository.findTagsInPostId(postId));
    }

    /** 댓글 등 다른 서비스가 게시글을 필요로 할 때 쓴다. */
    public Post findAlivePost(long postId) {
        Post post = postRepository.findPostWithWriter(postId)
                .orElseThrow(() -> new PostException(PostMessage.NOT_FOUND_POST));

        if (post.isDelete()) {
            throw new PostException(PostMessage.IS_DELETE_POST);
        }

        return post;
    }

    private void validateTags(PostRequest postRequest) {
        if (postRequest.getTags() != null && postRequest.getTags().length > MAX_TAGS) {
            throw new PostException("태그는 최대 " + MAX_TAGS + "개까지 등록할 수 있습니다.");
        }
    }

    private String nullToNormal(String type) {
        return type == null ? "normal" : type;
    }
}
