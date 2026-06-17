package com.junmo.certificatesystem.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.junmo.certificatesystem.entity.User;

/**
 * 로그인한 사용자 1명의 정보를 Spring Security가 이해할 수 있는 형태로 감싼 클래스.
 *
 * FR-02에서 중요한 부분은 getAuthorities():
 * - DB의 role(USER/ADMIN)을 "ROLE_USER", "ROLE_ADMIN" 문자열로 변환
 * - SecurityConfig의 hasRole("ADMIN") 검사에 사용됨
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /** 화면에서 이름, 입사일 등 추가 정보가 필요할 때 사용 */
    public User getUser() {
        return user;
    }

    /**
     * 현재 로그인 사용자의 권한 목록.
     * 예) role=ADMIN -> ROLE_ADMIN
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // DB에 저장된 SHA-256 해시
    }

    @Override
    public String getUsername() {
        return user.getUserId(); // 로그인 ID (예: jmjin, admin)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
