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

    // 微信授权登录 - 返回userid
    @PostMapping("/wechatLogin")
    public Object wechatLogin(@RequestParam("openid") String openid,
                              @RequestParam("nickname") String nickname,
                              @RequestParam("avatar") String avatar) {
        try {
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