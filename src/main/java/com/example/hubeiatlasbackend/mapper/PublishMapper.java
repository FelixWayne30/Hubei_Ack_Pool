package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface PublishMapper {
    @Select("INSERT INTO \"public\".\"maps\" (\"title\",\"type\", \"width\", \"height\") " +
            "VALUES (#{mapName},#{type},#{width},#{height}) RETURNING map_id::text  ")
    String insertBaseMapInfo(@Param("mapName") String mapName, @Param("type") String type, @Param("width") int width, @Param("height") int height);

    @Delete("DELETE FROM maps WHERE map_id =#{id} ;\n")
    void deleteMap(@Param("id") UUID id);

    @Update("UPDATE maps SET width = #{width}, height = #{height} WHERE map_id = #{id}")
    void editMapFileInfo(@Param("id") UUID id, @Param("width") int width, @Param("height") int height);

    @Update("UPDATE maps SET title = #{name}, description = #{description} WHERE map_id = #{id}")
    void editMapInfo(@Param("id") UUID id, @Param("name") String name, @Param("description") String description);
}
