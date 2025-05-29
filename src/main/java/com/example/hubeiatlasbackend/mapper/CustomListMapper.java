package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface CustomListMapper {

    // 获取用户的所有自定义列表
    @Select("SELECT list_id, name, description, create_time, update_time, " +
            "(SELECT COUNT(*) FROM list_maps WHERE list_id = custom_lists.list_id) as map_count " +
            "FROM custom_lists WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Map<String, Object>> getUserLists(@Param("userId") UUID userId);

    // 获取自定义列表详情
    @Select("SELECT list_id, name, description, create_time, update_time " +
            "FROM custom_lists WHERE list_id = #{listId}")
    Map<String, Object> getListById(@Param("listId") UUID listId);

    // 获取列表中的地图
    @Select("SELECT m.*, lm.add_time FROM maps m " +
            "JOIN list_maps lm ON m.map_id = lm.map_id " +
            "WHERE lm.list_id = #{listId} ORDER BY lm.add_time DESC")
    List<Map<String, Object>> getListMaps(@Param("listId") UUID listId);

    // 创建自定义列表
    @Insert("INSERT INTO custom_lists (user_id, name, description) " +
            "VALUES (#{userId}, #{name}, #{description}) RETURNING list_id")
    UUID createList(@Param("userId") UUID userId,
                    @Param("name") String name,
                    @Param("description") String description);

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

    // 检查地图是否已在列表中
    @Select("SELECT COUNT(*) FROM list_maps WHERE list_id = #{listId} AND map_id = #{mapId}")
    int checkMapInList(@Param("listId") UUID listId, @Param("mapId") UUID mapId);

    // 添加地图到列表
    @Insert("INSERT INTO list_maps (list_id, map_id) VALUES (#{listId}, #{mapId}) " +
            "ON CONFLICT (list_id, map_id) DO NOTHING")
    void addMapToList(@Param("listId") UUID listId, @Param("mapId") UUID mapId);

    // 从列表中移除地图
    @Delete("DELETE FROM list_maps WHERE list_id = #{listId} AND map_id = #{mapId}")
    int removeMapFromList(@Param("listId") UUID listId, @Param("mapId") UUID mapId);

    // 检查列表归属权
    @Select("SELECT user_id FROM custom_lists WHERE list_id = #{listId}")
    UUID getListOwner(@Param("listId") UUID listId);
}