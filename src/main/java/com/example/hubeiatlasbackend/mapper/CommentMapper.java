package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface CommentMapper {

    @Insert("insert into comments (\"user_id\",\"map_id\",\"content\") values(#{userId},#{mapId},#{comment})")
    void addComment(@Param("userId") UUID userId, @Param("mapId") UUID mapId,@Param("comment") String comment);

    @Select("SELECT\n" +
            "  comments.comment_id,\n" +
            "  maps.title AS mapname,\n" +
            "  users.nickname AS username,\n" +
            "  comments.content,\n" +
            "  TO_CHAR(comments.create_time, 'YYYY年MM月DD日HH24:MI:SS') AS commentTime,\n" +
            "  comments.status\n" +
            "FROM comments\n" +
            "LEFT JOIN maps ON comments.map_id = maps.map_id\n" +
            "LEFT JOIN users ON comments.user_id = users.user_id;")
    List<Map<String, Objects>> getComments();

    @Update("update comments set status = #{status} where comment_id = #{commentId}")
    void review(@Param("commentId") UUID commentId, @Param("status") Integer status);
}
