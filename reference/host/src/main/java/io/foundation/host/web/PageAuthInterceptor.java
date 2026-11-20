package io.foundation.host.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class PageAuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_ADMIN = "foundation.admin";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ADMIN))) {
            return true;
        }
        response.sendRedirect("/login");
        return false;
    }
}
