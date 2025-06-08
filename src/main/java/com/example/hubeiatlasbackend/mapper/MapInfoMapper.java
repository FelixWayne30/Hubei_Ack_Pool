package com.example.hubeiatlasbackend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mapper
public interface MapInfoMapper {

    @Select("SELECT * FROM maps " +
            "WHERE title ILIKE #{query} " +
            "OR description ILIKE #{query} " +
            "OR type ILIKE #{query} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Objects>> searchMaps(@Param("query") String query,
                                          @Param("limit") int limit,
                                          @Param("offset") int offset);

    @Select("select * from topics ORDER BY sort_order")
    List<Map<String, Objects>> getTopics();

    @Select("select * from maps ORDER BY create_time")
    List<Map<String, Objects>> getMaps();

    @Select("select * from topic_map a left join maps b on a.map_id = b.map_id\n" +
            "where a.topic_id = #{group_id} \n" +
            "ORDER BY a.map_rank")
    List<Map<String, Objects>> getMapsByGroupId(@Param("group_id") UUID group_id);

    @Select("select * from maps where map_id = #{map_id}")
    List<Map<String, Objects>> getMapsByMapId(@Param("map_id") UUID mapId);

    @Update("update topic_map set map_rank = #{mapRank} where topic_id = #{topicId} and map_id = #{mapId}")
    void updateMapOrder(@Param("topicId") UUID topicId, @Param("mapId") UUID mapId,@Param("mapRank") int mapRank);

    @Insert("insert into topic_map(\"topic_id\",\"map_id\",\"map_rank\") values(#{topicId},#{mapId},#{mapRank})")
    void addMaptoTopic(@Param("topicId") UUID topicId, @Param("mapId") UUID mapId,@Param("mapRank") int mapRank);

    @Select("delete from topic_map where topic_id = #{topicId} and map_id = #{mapId} and map_rank = #{mapRank}")
    void removeMapfromTopic(@Param("topicId") UUID topicId, @Param("mapId") UUID mapId,@Param("mapRank") int mapRank);

    @Update("""
        UPDATE topic_map
        SET map_rank = map_rank - 1
        WHERE topic_id = #{topicId}
          AND map_rank > #{mapRank}
    """)
    void shiftMapRanksAfterDelete(@Param("topicId") UUID topicId, @Param("mapRank") int mapRank);

    @Delete("delete from topic_map where topic_id = #{topicId}")
    void removeAllMapsfromTopic(@Param("topicId") UUID topicId);

    @Insert("insert into topics(\"title\",\"description\",\"sort_order\") values(#{name},#{description},#{order}) ")
    void addTopic(@Param("name") String name, @Param("description") String description, @Param("order")int order);

    @Delete("delete from topics where topic_id = #{topicId}")
    void deleteTopic(@Param("topicId")UUID topicId);

    @Update("update topics set sort_order = #{order} where topic_id = #{topicId}")
    void updateGroupOrder(@Param("topicId")UUID topicId, @Param("order")int order);

    @Update("update topics set title = #{name}, description = #{description} where topic_id = #{topicId}")
    void editGroupInfo(@Param("topicId")UUID topicId, @Param("name")String name, @Param("description")String description);
    
    @Select("select * from banners a left join maps b on a.map_id = b.map_id\n" +
            "ORDER BY a.map_rank")
    List<Map<String, Objects>> getBannerMaps();
}
