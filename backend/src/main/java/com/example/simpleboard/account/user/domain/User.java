package com.example.simpleboard.account.user.domain;

import com.example.simpleboard.account.user.constant.UserRole;
import com.example.simpleboard.post.post.domain.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 회원. UserDetails 를 직접 구현해서 스프링 시큐리티의 principal 로 그대로 쓴다.
 *
 * 테이블 이름이 users 인 것은 user 가 여러 DB(H2 등)에서 예약어이기 때문이다.
 */
@Entity
@Getter
@Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userKey;

    /** 로그인 아이디. 화면에 보이는 사용자 식별자이기도 하다. */
    @Column(nullable = false, length = 30, unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate joinDate;

    @Column(nullable = false)
    private LocalDate lastLoginDate;

    @Builder.Default
    @OneToMany(mappedBy = "writer")
    private List<Post> posts = new ArrayList<>();

    public static User createGeneralUser(String userId, String email, String encodedPassword) {
        return User.builder()
                .userId(userId)
                .email(email)
                .password(encodedPassword)
                .role(UserRole.USER)
                .lastLoginDate(LocalDate.now())
                .build();
    }

    public void updateLoginDate() {
        this.lastLoginDate = LocalDate.now();
    }

    public boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.password);
    }

    public void addPost(Post post) {
        this.posts.add(post);
        post.setWriter(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getRole()));
    }

    /** 시큐리티가 말하는 username 은 우리의 로그인 아이디다. */
    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
