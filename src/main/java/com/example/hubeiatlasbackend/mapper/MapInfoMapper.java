package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface MapInfoMapper {

    @Select("select * from map")
    List<Map<String, Objects>> getAllMapInfoList();
}
