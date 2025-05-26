package com.example.hubeiatlasbackend.service;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.hubeiatlasbackend.mapper.MapInfoMapper;
import java.util.*;

@Service
@Slf4j
public class MapInfoService {

    @Resource
    private MapInfoMapper mapInfoMapper;

    public List<Map<String, Objects>> getTopics() {
        return mapInfoMapper.getTopics();
    }

    public List<Map<String, Objects>> getMaps() {
        return mapInfoMapper.getMaps();
    }

    public List<Map<String, Objects>> getMapsByGroupId(UUID group_id) { return mapInfoMapper.getMapsByGroupId(group_id); }

    public List<Map<String, Objects>> getMapsByMapId(UUID mapId) {
        return mapInfoMapper.getMapsByMapId(mapId);
    }

    public void updateMapOrder(UUID topicId, UUID mapId, int mapRank) {
        mapInfoMapper.updateMapOrder(topicId,mapId,mapRank);
    }

    public void addMaptoTopic(UUID topicId, UUID mapId, int mapRank) {
        mapInfoMapper.addMaptoTopic(topicId,mapId,mapRank);
    }

    public void removeMapfromTopic(UUID topicId, UUID mapId, int mapRank) {
        mapInfoMapper.removeMapfromTopic(topicId,mapId,mapRank);
        mapInfoMapper.shiftMapRanksAfterDelete(topicId, mapRank);
    }
}
