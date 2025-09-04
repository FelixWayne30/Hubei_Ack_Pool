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
    @Select("SELECT s.subitem_id, s.subitem_name, s.subitem_type, " +
//            "s.extends_xmin, s.extends_ymin, s.extends_xmax, s.extends_ymax, " +
            "s.parent_map_name, m.map_id, m.title as map_title, " +
            "m.description as map_description " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.parent_map_name = m.title " +
            "WHERE s.subitem_name = #{subitemName}")
    List<Map<String, Object>> getSubmapsBySubitemName(@Param("subitemName") String subitemName);

    /**
     * 获取所有子图信息，用于大模型分析
     */
    @Select("SELECT s.subitem_id, s.subitem_name, s.subitem_type, " +
//            "s.extends_xmin, s.extends_ymin, s.extends_xmax, s.extends_ymax, " +
            "s.parent_map_name, m.map_id, m.title as map_title " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.parent_map_name = m.title " +
            "WHERE s.subitem_name IS NOT NULL AND s.subitem_name != '' " +
            "ORDER BY s.parent_map_name, s.subitem_name")
    List<Map<String, Object>> getAllSubmapsInfo();

    /**
     * 根据地图名称获取所有子项
     */
    @Select("SELECT * FROM submaps WHERE parent_map_name = #{mapName}")
    List<Map<String, Object>> getSubmapsByMapName(@Param("mapName") String mapName);

    /**
     * 根据地图ID获取所有子项（通过maps表关联）
     */
    @Select("SELECT s.* FROM submaps s " +
            "INNER JOIN maps m ON s.parent_map_name = m.title " +
            "WHERE m.map_id = #{mapId}")
    List<Map<String, Object>> getSubmapsByMapId(@Param("mapId") UUID mapId);

    /**
     * 智能搜索：根据子项名称模糊匹配
     */
    @Select("SELECT DISTINCT s.subitem_id, s.subitem_name, s.parent_map_name, " +
            "m.map_id, m.title as map_title " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.parent_map_name = m.title " +
            "WHERE s.subitem_name ILIKE #{query} " +
            "ORDER BY s.parent_map_name " +
            "LIMIT #{limit}")
    List<Map<String, Object>> searchSubitemsByName(@Param("query") String query, @Param("limit") int limit);

    /**
     * 根据子项ID获取详细信息
     */
    @Select("SELECT s.*, m.map_id, m.title as map_title, m.description as map_description " +
            "FROM submaps s " +
            "LEFT JOIN maps m ON s.parent_map_name = m.title " +
            "WHERE s.subitem_id = #{subitemId}")
    Map<String, Object> getSubitemById(@Param("subitemId") UUID subitemId);

    /**
     * 获取指定地图的所有子项边界信息
     */
    @Select("SELECT subitem_name, subitem_type, " +
            "extends_xmin, \"extends_ymin\n\" as extends_ymin, " +
            "extends_xmax, extends_ymax " +
            "FROM submaps " +
            "WHERE parent_map_name = #{mapName} " +
            "AND extends_xmin IS NOT NULL")
    List<Map<String, Object>> getSubmapBoundsByMapName(@Param("mapName") String mapName);
}