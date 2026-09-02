package com.example.simpleboard.post.post.domain;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.common.config.BooleanConverter;
import com.example.simpleboard.post.comment.domain.Comment;
import com.example.simpleboard.post.post.dto.PostRequest;
import com.example.simpleboard.post.tag.domain.Tag;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 1100)
    private String content;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private boolean isPrivate;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private boolean isBlockComment;

    /** 실제로 지우지 않고 표시만 한다. 댓글·추천 기록이 함께 사라지면 안 되기 때문이다. */
    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private boolean isDelete;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime postDate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_user_key", nullable = false)
    private User writer;

    /** 이 글을 추천한 사람들. 같은 사람이 두 번 추천하지 못하게 하는 근거가 된다. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_recommendation",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_key"))
    private List<User> recommendation = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public static Post createPost(PostRequest postRequest) {
        Post post = Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .isPrivate(postRequest.isPrivatePost())
                .isBlockComment(postRequest.isBlockComment())
                .isDelete(false)
                .build();

        post.replaceTags(postRequest.getTags());

        return post;
    }

    public void updatePost(PostRequest postRequest) {
        this.title = postRequest.getTitle();
        this.content = postRequest.getContent();
        this.isPrivate = postRequest.isPrivatePost();
        this.isBlockComment = postRequest.isBlockComment();

        replaceTags(postRequest.getTags());
    }

    public void deletePost() {
        this.isDelete = true;
    }

    public void increaseViews() {
        this.views += 1;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public boolean isRecommendedBy(User user) {
        return recommendation.stream().anyMatch(each -> each.getUserKey().equals(user.getUserKey()));
    }

    public void addRecommendation(User user) {
        this.recommendation.add(user);
        this.likes += 1;
    }

    public boolean isWrittenBy(String userId) {
        return writer != null && writer.getUserId().equals(userId);
    }

    private void replaceTags(String[] tagList) {
        this.tags.clear();

        if (tagList == null) {
            return;
        }

        for (String tag : tagList) {
            if (tag != null && !tag.isBlank()) {
                this.tags.add(Tag.createTag(tag.trim()));
            }
        }
    }
}
