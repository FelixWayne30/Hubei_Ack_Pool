package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface CustomListMapper {

    // 获取用户的所有自定义列表
    @Select("SELECT list_id::text as list_id, name, description, create_time, " +
            "(SELECT COUNT(*) FROM list_maps WHERE list_id = custom_lists.list_id) as count " +
            "FROM custom_lists WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Map<String, Object>> getUserLists(@Param("userId") UUID userId);

    // 创建自定义列表 - 使用最简单的方案
    @Insert("INSERT INTO custom_lists (user_id, name, description) VALUES (#{userId}, #{name}, #{description})")
    void createList(@Param("userId") UUID userId,
                    @Param("name") String name,
                    @Param("description") String description);

    // 获取用户最新创建的列表ID - 返回String类型
    @Select("SELECT list_id::text FROM custom_lists WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 1")
    String getLatestListId(@Param("userId") UUID userId);

    // 更新自定义列表
    @Update("UPDATE custom_lists SET name = #{name}, description = #{description}, " +
            "update_time = CURRENT_TIMESTAMP WHERE list_id = #{listId} AND user_id = #{userId}")
    int updateList(@Param("listId") UUID listId,
                   @Param("userId") UUID userId,
                   @Param("name") String name,
                   @Param("description") String description);

    // 删除自定义列表
    @Delete("DELETE FROM custom_lists WHERE list_id = #{listId} AND user_id = #{userId}")
    int deleteList(@Param("listId") UUID listId, @Param("userId") UUID userId);

    // 检查列表所有权
    @Select("SELECT COUNT(*) FROM custom_lists WHERE list_id = #{listId} AND user_id = #{userId}")
    int checkListOwnership(@Param("listId") UUID listId, @Param("userId") UUID userId);

    // 获取列表详情
    @Select("SELECT cl.list_id::text as list_id, cl.name, cl.description, cl.create_time, " +
            "m.map_id::text as map_id, m.title, m.description as map_description, m.type, m.width, m.height, m.create_time as map_create_time " +
            "FROM custom_lists cl " +
            "LEFT JOIN list_maps lm ON cl.list_id = lm.list_id " +
            "LEFT JOIN maps m ON lm.map_id = m.map_id " +
            "WHERE cl.list_id = #{listId}")
    List<Map<String, Object>> getListDetail(@Param("listId") UUID listId);

    // 检查地图是否在列表中
    @Select("SELECT COUNT(*) FROM list_maps WHERE list_id = #{listId} AND map_id = #{mapId}")
    int checkMapInList(@Param("listId") UUID listId, @Param("mapId") UUID mapId);

    // 添加地图到列表
    @Insert("INSERT INTO list_maps (list_id, map_id) VALUES (#{listId}, #{mapId})")
    void addMapToList(@Param("listId") UUID listId, @Param("mapId") UUID mapId);

    // 从列表中移除地图
    @Delete("DELETE FROM list_maps WHERE list_id = #{listId} AND map_id = #{mapId}")
    int removeMapFromList(@Param("listId") UUID listId, @Param("mapId") UUID mapId);
}