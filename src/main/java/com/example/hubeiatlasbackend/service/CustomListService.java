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
        try {
            return customListMapper.getUserLists(userId);
        } catch (Exception e) {
            log.error("获取用户列表失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("获取列表失败");
        }
    }

    /**
     * 创建自定义列表
     */
    public Map<String, Object> createList(UUID userId, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("列表名称不能为空");
        }

        try {
            // 插入数据
            customListMapper.createList(userId, name.trim(), description);

            // 获取刚创建的列表ID - 现在返回String类型
            String listId = customListMapper.getLatestListId(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("list_id", listId);
            return result;
        } catch (Exception e) {
            log.error("创建列表失败: userId={}, name={}, error={}", userId, name, e.getMessage());
            throw new RuntimeException("创建列表失败");
        }
    }

    /**
     * 更新自定义列表
     */
    public boolean updateList(UUID listId, UUID userId, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("列表名称不能为空");
        }

        try {
            // 检查列表所有权
            if (customListMapper.checkListOwnership(listId, userId) == 0) {
                return false;
            }

            int updated = customListMapper.updateList(listId, userId, name.trim(), description);
            return updated > 0;
        } catch (Exception e) {
            log.error("更新列表失败: listId={}, userId={}, error={}", listId, userId, e.getMessage());
            throw new RuntimeException("更新列表失败");
        }
    }

    /**
     * 删除自定义列表
     */
    public boolean deleteList(UUID listId, UUID userId) {
        try {
            int deleted = customListMapper.deleteList(listId, userId);
            return deleted > 0;
        } catch (Exception e) {
            log.error("删除列表失败: listId={}, userId={}, error={}", listId, userId, e.getMessage());
            throw new RuntimeException("删除列表失败");
        }
    }

    /**
     * 获取自定义列表详情
     */
    public Map<String, Object> getListDetail(UUID listId) {
        try {
            List<Map<String, Object>> results = customListMapper.getListDetail(listId);

            if (results.isEmpty()) {
                return null;
            }

            // 构建列表详情
            Map<String, Object> listInfo = new HashMap<>();
            Map<String, Object> firstRow = results.get(0);

            listInfo.put("id", firstRow.get("list_id"));
            listInfo.put("name", firstRow.get("name"));
            listInfo.put("description", firstRow.get("description"));
            listInfo.put("create_time", firstRow.get("create_time"));

            // 构建地图列表
            List<Map<String, Object>> maps = new ArrayList<>();
            for (Map<String, Object> row : results) {
                if (row.get("map_id") != null) {
                    Map<String, Object> mapInfo = new HashMap<>();
                    mapInfo.put("map_id", row.get("map_id"));
                    mapInfo.put("title", row.get("title"));
                    mapInfo.put("description", row.get("map_description"));
                    mapInfo.put("type", row.get("type"));
                    mapInfo.put("width", row.get("width"));
                    mapInfo.put("height", row.get("height"));
                    mapInfo.put("create_time", row.get("map_create_time"));
                    maps.add(mapInfo);
                }
            }

            listInfo.put("maps", maps);
            return listInfo;
        } catch (Exception e) {
            log.error("获取列表详情失败: listId={}, error={}", listId, e.getMessage());
            throw new RuntimeException("获取列表详情失败");
        }
    }

    /**
     * 检查地图是否在列表中
     */
    public boolean isMapInList(UUID listId, UUID mapId) {
        try {
            int count = customListMapper.checkMapInList(listId, mapId);
            return count > 0;
        } catch (Exception e) {
            log.error("检查地图是否在列表中失败: listId={}, mapId={}, error={}", listId, mapId, e.getMessage());
            return false;
        }
    }

    /**
     * 添加地图到列表
     */
    public boolean addMapToList(UUID listId, UUID mapId, UUID userId) {
        try {
            // 检查列表所有权
            if (customListMapper.checkListOwnership(listId, userId) == 0) {
                return false;
            }

            // 检查是否已存在
            if (customListMapper.checkMapInList(listId, mapId) > 0) {
                return true; // 已存在，返回成功
            }

            customListMapper.addMapToList(listId, mapId);
            return true;
        } catch (Exception e) {
            log.error("添加地图到列表失败: listId={}, mapId={}, userId={}, error={}",
                    listId, mapId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * 从列表中移除地图
     */
    public boolean removeMapFromList(UUID listId, UUID mapId, UUID userId) {
        try {
            // 检查列表所有权
            if (customListMapper.checkListOwnership(listId, userId) == 0) {
                return false;
            }

            int removed = customListMapper.removeMapFromList(listId, mapId);
            return removed > 0;
        } catch (Exception e) {
            log.error("从列表中移除地图失败: listId={}, mapId={}, userId={}, error={}",
                    listId, mapId, userId, e.getMessage());
            return false;
        }
    }
}