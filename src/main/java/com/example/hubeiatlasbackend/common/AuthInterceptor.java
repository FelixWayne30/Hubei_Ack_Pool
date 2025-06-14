package com.example.hubeiatlasbackend.common;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${server.secret}")
    private String serverSecret;

    private boolean isAuthorized(HttpServletRequest request) {
        // 检查授权信息，如header中是不是有授权令牌
        String token = request.getHeader("Authorization");
        return serverSecret.equals(token);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 检查是不是需要放行
//        String uri = request.getRequestURI();
//        // 举个例子，若有若干无需授权的白名单：
//        if (uri.startsWith("/login") ||
//                uri.startsWith("/public") ||
//                uri.startsWith("/protected-pictures")) { //若你希望图片自己进行授权
//            return true;
//        }
//     设置图片白名单
      String uri = request.getRequestURI();
        if (uri.startsWith("/image/")) {
            return true;
        }

        if (!isAuthorized(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("403 Forbidden: Invalid Token");

            return false;
        }
        return true;
    }
}

