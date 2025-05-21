package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.util.Objects;

public interface PublishMapper {
    @Select("INSERT INTO \"public\".\"maps\" (\"title\",\"type\", \"width\", \"height\") " +
            "VALUES (#{mapName},#{type},#{width},#{height}) RETURNING map_id::text  ")
    String insertBaseMapInfo(@Param("mapName") String mapName, @Param("type") String type, @Param("width") int width, @Param("height") int height);
}
