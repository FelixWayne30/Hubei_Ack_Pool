// src/main/java/com/example/hubeiatlasbackend/mapper/UserMapper.java
package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface UserMapper {

    // 通过openid查找用户
    @Select("SELECT user_id, nickname, avatar, create_time, last_login_time, status " +
            "FROM users WHERE openid = #{openid}")
    Map<String, Object> getUserByOpenid(@Param("openid") String openid);

    // 通过userid获取用户信息
    @Select("SELECT user_id, nickname, avatar, create_time, last_login_time, status " +
            "FROM users WHERE user_id = #{userId}")
    Map<String, Object> getUserInfo(@Param("userId") UUID userId);

    // 创建微信用户（不返回UUID）
    @Insert("INSERT INTO users (nickname, avatar, openid, last_login_time) " +
            "VALUES (#{nickname}, #{avatar}, #{openid}, CURRENT_TIMESTAMP)")
    void createWechatUserNoReturn(@Param("openid") String openid,
                                  @Param("nickname") String nickname,
                                  @Param("avatar") String avatar);

    // 更新用户登录信息
    @Update("UPDATE users SET nickname = #{nickname}, avatar = #{avatar}, " +
            "last_login_time = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    void updateUserLoginInfo(@Param("userId") UUID userId,
                             @Param("nickname") String nickname,
                             @Param("avatar") String avatar);
}