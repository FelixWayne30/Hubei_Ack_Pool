package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DownloadMapper {

    @Insert("INSERT INTO download_requests (map_id, user_id, email, reason, create_time) " +
            "VALUES (#{mapId}, #{userId}, #{email}, #{reason}, NOW())")
    void addRequest(@Param("mapId") UUID mapId,
                   @Param("userId") UUID userId,
                   @Param("email")  String email,
                   @Param("reason") String reason);

    @Select("SELECT " +
            "dr.request_id, " +
            "dr.map_id, " +
            "dr.user_id, " +
            "dr.email, " +
            "dr.reason, " +
            "dr.status, " +
            "dr.create_time, " +
            "m.title AS map_title " +
            "FROM download_requests dr " +
            "JOIN maps m ON dr.map_id = m.id")
    List<Map<String, Object>> getRequests();

    @Update("update comments set status = #{status} where request_id = #{requestID}")
    void review(@Param("requestID") UUID requestID, @Param("status") Integer status);
}
