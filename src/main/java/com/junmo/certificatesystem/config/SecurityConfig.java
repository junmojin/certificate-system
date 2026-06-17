package com.junmo.certificatesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.junmo.certificatesystem.security.CustomUserDetailsService;
import com.junmo.certificatesystem.security.RoleBasedAuthenticationSuccessHandler;
import com.junmo.certificatesystem.security.Sha256PasswordEncoder;

import lombok.RequiredArgsConstructor;

/**
 * FR-02 접근제어의 핵심 설정 클래스.
 *
 * [역할]
 * 1) 어떤 URL을 누가 접근할 수 있는지 규칙 정의
 * 2) 로그인/로그아웃 처리 방식 정의
 * 3) 권한 없는 접근 시 이동할 페이지 정의
 */
@Configuration
@EnableWebSecurity          // Spring Security 활성화
@EnableMethodSecurity       // @PreAuthorize 같은 메서드 단위 권한 검사 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    // DB에서 사용자 정보를 읽어오는 서비스 (FR-01 로그인에서 사용)
    private final CustomUserDetailsService customUserDetailsService;

    // 로그인 성공 후 USER/ADMIN 화면을 나눠서 보내주는 핸들러 (FR-02)
    private final RoleBasedAuthenticationSuccessHandler successHandler;

    /** 비밀번호 검증 방식 등록 (CSV의 SHA-256 해시와 비교) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Sha256PasswordEncoder();
    }

    /**
     * 실제 로그인 검증 담당.
     * - CustomUserDetailsService: DB에서 user_id 조회
     * - PasswordEncoder: 입력 비밀번호와 DB 해시 비교
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * 권한이 없는 URL 접근 시 처리.
     * 예: USER가 /admin/home 접근 -> /access-denied 로 이동
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                response.sendRedirect(request.getContextPath() + "/access-denied");
    }

    /**
     * 보안 규칙 전체를 한 번에 설정하는 메서드.
     * 요청이 들어올 때마다 이 규칙을 기준으로 통과/차단을 결정한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                // 로그인 없이 접근 가능한 URL
                .requestMatchers("/login", "/css/**", "/js/**", "/access-denied").permitAll()

                // 관리자 전용 URL (/admin/home, /admin/certificates 등)
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 일반 사용자 전용 URL
                .requestMatchers("/home", "/career", "/certificate").hasRole("USER")

                // 사용자 + 관리자 공통 URL
                .requestMatchers("/boards", "/boards/**").hasAnyRole("USER", "ADMIN")

                // 위에 없는 나머지 URL은 로그인만 되어 있으면 접근 가능
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")              // 로그인 화면 URL (GET)
                .loginProcessingUrl("/login")     // 로그인 처리 URL (POST)
                .usernameParameter("username")    // login.html의 ID input name
                .passwordParameter("password")    // login.html의 PW input name
                .successHandler(successHandler)   // 로그인 성공 시 USER/ADMIN 분기
                .failureUrl("/login?error")       // 로그인 실패 시
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler) // 권한 없음 처리
            );

        return http.build();
    }
}
