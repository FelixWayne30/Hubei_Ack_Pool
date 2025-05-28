// src/main/java/com/example/hubeiatlasbackend/controller/UserController.java
package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    // 修改为接收JSON格式的请求
    @PostMapping("/wechatLogin")
    public Object wechatLogin(@RequestBody Map<String, Object> requestBody) {
        try {
            String openid = (String) requestBody.get("openid");
            String nickname = (String) requestBody.get("nickname");
            String avatar = (String) requestBody.get("avatar");

            // 可选：记录code，但不使用
            // String code = (String) requestBody.get("code");

            Map<String, Object> userInfo = userService.wechatLogin(openid, nickname, avatar);
            return renderSuccess("登录成功", userInfo);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    // 通过userid获取用户信息
    @GetMapping("/info/{userId}")
    public Object getUserInfo(@PathVariable("userId") UUID userId) {
        try {
            Map<String, Object> userInfo = userService.getUserInfo(userId);
            if (userInfo == null) {
                return renderError("用户不存在");
            }
            return renderSuccess(userInfo);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }
}