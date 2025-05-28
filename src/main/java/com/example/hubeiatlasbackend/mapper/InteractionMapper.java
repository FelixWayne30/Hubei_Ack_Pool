package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface InteractionMapper {

    // 检查用户是否已收藏地图
    @Select("SELECT COUNT(1) FROM collections WHERE user_id = #{userId} AND map_id = #{mapId}")
    int checkCollectionExists(@Param("userId") UUID userId, @Param("mapId") UUID mapId);

    // 添加收藏
    @Insert("INSERT INTO collections (user_id, map_id, create_time) VALUES (#{userId}, #{mapId}, CURRENT_TIMESTAMP)")
    void addCollection(@Param("userId") UUID userId, @Param("mapId") UUID mapId);

    // 删除收藏
    @Delete("DELETE FROM collections WHERE user_id = #{userId} AND map_id = #{mapId}")
    void removeCollection(@Param("userId") UUID userId, @Param("mapId") UUID mapId);

    // 获取用户的所有收藏
    @Select("SELECT c.collection_id, c.create_time, m.* FROM collections c JOIN maps m ON c.map_id = m.map_id WHERE c.user_id = #{userId} ORDER BY c.create_time DESC")
    List<Map<String, Object>> getUserCollections(@Param("userId") UUID userId);

    // 检查用户是否已点赞地图
    @Select("SELECT COUNT(1) FROM likes WHERE user_id = #{userId} AND map_id = #{mapId}")
    int checkLikeExists(@Param("userId") UUID userId, @Param("mapId") UUID mapId);

    // 添加点赞
    @Insert("INSERT INTO likes (user_id, map_id, create_time) VALUES (#{userId}, #{mapId}, CURRENT_TIMESTAMP)")
    void addLike(@Param("userId") UUID userId, @Param("mapId") UUID mapId);

    // 删除点赞
    @Delete("DELETE FROM likes WHERE user_id = #{userId} AND map_id = #{mapId}")
    void removeLike(@Param("userId") UUID userId, @Param("mapId") UUID mapId);

    // 更新地图点赞数
    @Update("UPDATE maps SET like_count = (SELECT COUNT(1) FROM likes WHERE map_id = #{mapId}) WHERE map_id = #{mapId}")
    void updateMapLikeCount(@Param("mapId") UUID mapId);
}