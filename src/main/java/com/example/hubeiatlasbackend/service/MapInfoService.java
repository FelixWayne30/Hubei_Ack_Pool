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

    public void removeAllMapsfromTopic(UUID topicId) {
        mapInfoMapper.removeAllMapsfromTopic(topicId);
    }

    public void addTopic(String name, String description, int order) {
        mapInfoMapper.addTopic(name,description,order);
    }

    public void deleteTopic(UUID topicId) {
        mapInfoMapper.deleteTopic(topicId);
    }

    public void updateGroupOrder(UUID topicId, int order) {
        mapInfoMapper.updateGroupOrder(topicId, order);
    }

    public List<Map<String, Objects>> searchMaps(String query, int page, int size) {
        String searchQuery = "%" + query + "%";
        int offset = (page - 1) * size;
        return mapInfoMapper.searchMaps(searchQuery, size, offset);
    }

    public void editGroupInfo(UUID topicId, String name, String description) {
        mapInfoMapper.editGroupInfo(topicId,name,description);
    }
    public List<Map<String, Objects>> getBannerMaps() {
        return mapInfoMapper.getBannerMaps();
    }

    public void updateBannerMapOrder(UUID mapId, int mapRank) {
        mapInfoMapper.updateBannerMapOrder(mapId,mapRank);
    }

    public void addMaptoBanner(UUID mapId, int mapRank) {
        mapInfoMapper.addMaptoBanner(mapId,mapRank);
    }

    public void removeMapfromBanner(UUID mapId, int mapRank) {
        mapInfoMapper.removeMapfromBanner(mapId,mapRank);
        mapInfoMapper.shiftBannerMapRanksAfterDelete(mapRank);
    }
}
