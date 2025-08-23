package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;

import java.util.UUID;

public interface PublishMapper {
    @Select("INSERT INTO \"public\".\"maps\" (\"title\",\"type\", \"width\", \"height\") " +
            "VALUES (#{mapName},#{type},#{width},#{height}) RETURNING map_id::text  ")
    String insertBaseMapInfo(@Param("mapName") String mapName, @Param("type") String type, @Param("width") int width, @Param("height") int height);

    @Delete("DELETE FROM maps WHERE map_id = #{id}; DELETE FROM submaps WHERE subitem_id = #{id}")
    String deleteMap(@Param("id") UUID id);

    @Update("UPDATE maps SET width = #{width}, height = #{height} WHERE map_id = #{id}")
    void editMapFileInfo(@Param("id") UUID id, @Param("width") int width, @Param("height") int height);

    @Update("UPDATE maps SET title = #{name}, description = #{description} WHERE map_id = #{id}")
    void editMapInfo(@Param("id") UUID id, @Param("name") String name, @Param("description") String description);

    @Insert("INSERT INTO \"public\".\"maps\" (\"title\", \"origin_topic\", \"origin_subtopic\") " +
            "VALUES (#{mapName}, #{originTopic}, #{subTopic})")
    void insertBaseMapInfoSheet(@Param("mapName") String mapName,
                                  @Param("originTopic") String originTopic,
                                  @Param("subTopic") String subTopic);

    @Insert("INSERT INTO \"public\".\"submaps\" (\"subitem_name\", \"subitem_type\", \"parent_map_name\") " +
            "VALUES (#{mapName}, #{subitem}, #{parentMapName})")
    void insertBaseMapInfoPiece(@Param("mapName") String mapName,
                                  @Param("subitem") String subitem,
                                  @Param("parentMapName") String parentMapName);
}
