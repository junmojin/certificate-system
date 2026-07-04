package com.junmo.certificatesystem.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.junmo.certificatesystem.service.audit.ActivityLogRecorder;
import com.junmo.certificatesystem.service.audit.ActivityMenuResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuAccessLoggingInterceptor implements HandlerInterceptor {

    private final ActivityMenuResolver activityMenuResolver;
    private final ActivityLogRecorder activityLogRecorder;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String uri = normalizeUri(request);
        if (uri.equals("/login") || uri.equals("/logout") || uri.equals("/admin/activity-logs")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return true;
        }

        activityMenuResolver.resolve(request.getMethod(), uri)
                .ifPresent(menuName -> activityLogRecorder.record(authentication.getName(), menuName));

        return true;
    }

    private String normalizeUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }
}
