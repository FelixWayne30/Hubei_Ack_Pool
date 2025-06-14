package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface SubmapsMapper {

    /**
     * 获取所有子项名称，用于大模型分析匹配
     */
    @Select("SELECT DISTINCT subitem_name FROM submaps WHERE subitem_name IS NOT NULL AND subitem_name != ''")
    List<String> getAllSubitemNames();

    /**
     * 根据子项名称获取完整的子图信息（包含关联的地图信息）
     */
    @Select("SELECT s.subitem_name, s.shubitem_type, s.extends_xmin, s.extends_ymin, " +
            "s.extends_xmax, s.extends_ymax, s.map_id, m.title as map_title, " +
            "m.description as map_description " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.map_id = m.map_id " +
            "WHERE s.subitem_name = #{subitemName}")
    List<Map<String, Object>> getSubmapsBySubitemName(@Param("subitemName") String subitemName);

    /**
     * 获取所有子图信息（包含子项名称、类型等），用于大模型分析
     */
    @Select("SELECT s.subitem_name, s.shubitem_type, s.extends_xmin, s.extends_ymin, " +
            "s.extends_xmax, s.extends_ymax, s.map_id, m.title as map_title " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.map_id = m.map_id " +
            "WHERE s.subitem_name IS NOT NULL AND s.subitem_name != '' " +
            "ORDER BY m.title, s.subitem_name")
    List<Map<String, Object>> getAllSubmapsInfo();

    /**
     * 根据地图ID获取所有子项
     */
    @Select("SELECT * FROM submaps WHERE map_id = #{mapId}")
    List<Map<String, Object>> getSubmapsByMapId(@Param("mapId") UUID mapId);

    /**
     * 智能搜索相关：根据子项名称模糊匹配
     */
    @Select("SELECT DISTINCT s.subitem_name, s.map_id, m.title as map_title " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.map_id = m.map_id " +
            "WHERE s.subitem_name ILIKE #{query} " +
            "ORDER BY m.title " +
            "LIMIT #{limit}")
    List<Map<String, Object>> searchSubitemsByName(@Param("query") String query, @Param("limit") int limit);
}