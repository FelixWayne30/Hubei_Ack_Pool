package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface MapExtendsMapper {

    /**
     * 获取所有子项名称，用于大模型分析匹配
     */
    @Select("SELECT DISTINCT subitem_name FROM map_extends WHERE subitem_name IS NOT NULL AND subitem_name != ''")
    List<String> getAllSubitemNames();

    /**
     * 根据子项名称获取完整的地图扩展信息
     */
    @Select("SELECT * FROM map_extends WHERE subitem_name = #{subitemName}")
    List<Map<String, Object>> getMapExtendsBySubitemName(@Param("subitemName") String subitemName);

    /**
     * 获取所有地图扩展信息（包含子项名称、类型等），用于大模型分析,后续用于前端跳转缩放
     */
    @Select("SELECT map_sheet_name, subitem_name, shubitem_type, " +
            "extends_xmin, extends_ymin, extends_xmax, extends_ymax " +
            "FROM map_extends " +
            "WHERE subitem_name IS NOT NULL AND subitem_name != '' " +
            "ORDER BY map_sheet_name, subitem_name")
    List<Map<String, Object>> getAllMapExtendsInfo();


    /**
     * 根据地图表名获取所有子项
     */
    @Select("SELECT * FROM map_extends WHERE map_sheet_name = #{mapSheetName}")
    List<Map<String, Object>> getMapExtendsBySheetName(@Param("mapSheetName") String mapSheetName);
}