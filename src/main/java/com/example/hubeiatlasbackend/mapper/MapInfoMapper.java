package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface MapInfoMapper {
    @Select("select topic_id,title from topics")
    List<Map<String, Objects>> getTopics();

    @Select("select * from maps where topic_id = uuid(#{group_id})")
    List<Map<String, Objects>> getMapsByGroupId(@Param("group_id") String group_id);
}
