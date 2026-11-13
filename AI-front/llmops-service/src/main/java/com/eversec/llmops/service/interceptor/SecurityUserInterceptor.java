package com.eversec.llmops.service.interceptor;

import cn.hutool.core.util.URLUtil;
import com.eversec.llmops.service.service.AbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SecurityUserInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_NAME_KEY = "username";
    public static final String HEADER_AB_USER_ID_KEY = "ab_user_id";

    public static final String HEADER_AB_USER_NAME_KEY = "ab_user_name";

    @Autowired
    private AbUserService abUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (HttpMethod.POST.name().equalsIgnoreCase(method) ||
                HttpMethod.PUT.name().equalsIgnoreCase(method) ||
                HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
            String username = request.getHeader(HEADER_USER_NAME_KEY);
            if (username != null && !username.isEmpty()) {
                String abUserName = URLUtil.decode(username);
                Integer abUserId = abUserService.getUserIdByUsername(abUserName);
                if (abUserId != null) {
                    request.setAttribute(HEADER_AB_USER_ID_KEY, abUserId);
                    request.setAttribute(HEADER_AB_USER_NAME_KEY, abUserName);
                }
            }
        }
        return true;
    }
}