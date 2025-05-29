package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.CustomListMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class CustomListService {

    @Resource
    private CustomListMapper customListMapper;

    /**
     * 获取用户的所有自定义列表
     */
    public List<Map<String, Object>> getUserLists(UUID userId) {
        return customListMapper.getUserLists(userId);
    }

    /**
     * 获取自定义列表详情
     */
    public Map<String, Object> getListDetail(UUID listId) {
        Map<String, Object> listDetail = customListMapper.getListById(listId);
        if (listDetail != null) {
            List<Map<String, Object>> maps = customListMapper.getListMaps(listId);
            listDetail.put("maps", maps);
            listDetail.put("mapCount", maps.size());
        }
        return listDetail;
    }

    /**
     * 创建自定义列表
     */
    public UUID createList(UUID userId, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("列表名称不能为空");
        }

        return customListMapper.createList(userId, name, description);
    }

    /**
     * 更新自定义列表
     */
    public boolean updateList(UUID listId, UUID userId, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("列表名称不能为空");
        }

        // 检查列表所有权
        UUID listOwner = customListMapper.getListOwner(listId);
        if (listOwner == null || !listOwner.equals(userId)) {
            return false;
        }

        int updated = customListMapper.updateList(listId, userId, name, description);
        return updated > 0;
    }

    /**
     * 删除自定义列表
     */
    public boolean deleteList(UUID listId, UUID userId) {
        // 检查列表所有权
        UUID listOwner = customListMapper.getListOwner(listId);
        if (listOwner == null || !listOwner.equals(userId)) {
            return false;
        }

        int deleted = customListMapper.deleteList(listId, userId);
        return deleted > 0;
    }

    /**
     * 添加地图到列表
     */
    public boolean addMapToList(UUID listId, UUID mapId, UUID userId) {
        // 检查列表所有权
        UUID listOwner = customListMapper.getListOwner(listId);
        if (listOwner == null || !listOwner.equals(userId)) {
            return false;
        }

        customListMapper.addMapToList(listId, mapId);
        return true;
    }

    /**
     * 批量添加地图到列表
     */
    @Transactional
    public boolean addMapsToList(UUID listId, List<UUID> mapIds, UUID userId) {
        // 检查列表所有权
        UUID listOwner = customListMapper.getListOwner(listId);
        if (listOwner == null || !listOwner.equals(userId)) {
            return false;
        }

        for (UUID mapId : mapIds) {
            customListMapper.addMapToList(listId, mapId);
        }
        return true;
    }

    /**
     * 从列表中移除地图
     */
    public boolean removeMapFromList(UUID listId, UUID mapId, UUID userId) {
        // 检查列表所有权
        UUID listOwner = customListMapper.getListOwner(listId);
        if (listOwner == null || !listOwner.equals(userId)) {
            return false;
        }

        int removed = customListMapper.removeMapFromList(listId, mapId);
        return removed > 0;
    }

    /**
     * 批量从列表中移除地图
     */
    @Transactional
    public boolean removeMapsFromList(UUID listId, List<UUID> mapIds, UUID userId) {
        // 检查列表所有权
        UUID listOwner = customListMapper.getListOwner(listId);
        if (listOwner == null || !listOwner.equals(userId)) {
            return false;
        }

        boolean success = true;
        for (UUID mapId : mapIds) {
            int removed = customListMapper.removeMapFromList(listId, mapId);
            if (removed <= 0) {
                success = false;
            }
        }
        return success;
    }

    /**
     * 检查地图是否在列表中
     */
    public boolean isMapInList(UUID listId, UUID mapId) {
        int count = customListMapper.checkMapInList(listId, mapId);
        return count > 0;
    }
}