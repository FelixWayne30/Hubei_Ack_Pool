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
//            log.info("用户登录更新: {}", nickname);
            // 返回更新后的信息
            return userMapper.getUserInfo(userId);
        } else {
            // 新用户，创建记录并返回userid
            UUID newUserId = userMapper.createWechatUser(openid, nickname, avatar);
//            log.info("新用户创建: {}, userId: {}", nickname, newUserId);
            return userMapper.getUserInfo(newUserId);
        }
    }

    public Map<String, Object> getUserInfo(UUID userId) {
        return userMapper.getUserInfo(userId);
    }
}