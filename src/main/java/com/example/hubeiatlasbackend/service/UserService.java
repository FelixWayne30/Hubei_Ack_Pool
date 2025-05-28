// src/main/java/com/example/hubeiatlasbackend/service/UserService.java
package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Resource
    private UserMapper userMapper;

    public Map<String, Object> wechatLogin(String openid, String nickname, String avatar) {
        // 通过openid查找用户
        Map<String, Object> existingUser = userMapper.getUserByOpenid(openid);

        if (existingUser != null) {
            // 用户已存在，更新信息
            UUID userId = (UUID) existingUser.get("user_id");
            userMapper.updateUserLoginInfo(userId, nickname, avatar);
            // 返回更新后的信息
            return userMapper.getUserInfo(userId);
        } else {
            // 新用户，创建记录
            userMapper.createWechatUserNoReturn(openid, nickname, avatar);
            // 查询新创建的用户信息并返回
            return userMapper.getUserByOpenid(openid);
        }
    }

    public Map<String, Object> getUserInfo(UUID userId) {
        return userMapper.getUserInfo(userId);
    }
}