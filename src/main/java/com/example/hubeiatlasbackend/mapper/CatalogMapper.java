package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CatalogMapper {

    @Select("SELECT \"group\", subgroup, map FROM catalogs ORDER BY \"group\", subgroup, map")
    List<Map<String, Object>> getCatalogs();
}