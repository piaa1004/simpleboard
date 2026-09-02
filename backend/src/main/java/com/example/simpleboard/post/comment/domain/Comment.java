package com.example.simpleboard.post.comment.domain;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.common.config.BooleanConverter;
import com.example.simpleboard.post.post.domain.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_user_key", nullable = false)
    private User writer;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private boolean isDelete;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime postDate;

    public static Comment createComment(String content, User writer) {
        return Comment.builder()
                .content(content)
                .writer(writer)
                .isDelete(false)
                .build();
    }

    public void deleteComment() {
        this.isDelete = true;
    }

    public boolean isWrittenBy(String userId) {
        return writer != null && writer.getUserId().equals(userId);
    }
}
