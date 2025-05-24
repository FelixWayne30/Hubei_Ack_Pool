package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface MapInfoMapper {
    @Select("select * from topics ORDER BY sort_order")
    List<Map<String, Objects>> getTopics();

    @Select("select * from maps ORDER BY create_time")
    List<Map<String, Objects>> getMaps();

    @Select("select * from maps where topic_id = uuid(#{group_id})")
    List<Map<String, Objects>> getMapsByGroupId(@Param("group_id") String group_id);
}
