package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.InteractionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class InteractionService {

    @Resource
    private InteractionMapper interactionMapper;

    /**
     * 切换收藏状态
     * @param userId 用户ID
     * @param mapId 地图ID
     * @return 包含当前收藏状态的Map
     */
    @Transactional
    public Map<String, Object> toggleCollection(UUID userId, UUID mapId) {
        int exists = interactionMapper.checkCollectionExists(userId, mapId);

        Map<String, Object> result = new HashMap<>();
        if (exists > 0) {
            // 已收藏，取消收藏
            interactionMapper.removeCollection(userId, mapId);
            result.put("collected", false);
        } else {
            // 未收藏，添加收藏
            interactionMapper.addCollection(userId, mapId);
            result.put("collected", true);
        }

        return result;
    }

    /**
     * 获取用户的收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    public List<Map<String, Object>> getUserCollections(UUID userId) {
        return interactionMapper.getUserCollections(userId);
    }

    /**
     * 检查用户是否已收藏地图
     * @param userId 用户ID
     * @param mapId 地图ID
     * @return 是否已收藏
     */
    public boolean checkIsCollected(UUID userId, UUID mapId) {
        int exists = interactionMapper.checkCollectionExists(userId, mapId);
        return exists > 0;
    }

    /**
     * 切换点赞状态
     * @param userId 用户ID
     * @param mapId 地图ID
     * @return 包含当前点赞状态的Map
     */
    @Transactional
    public Map<String, Object> toggleLike(UUID userId, UUID mapId) {
        int exists = interactionMapper.checkLikeExists(userId, mapId);

        Map<String, Object> result = new HashMap<>();
        if (exists > 0) {
            // 已点赞，取消点赞
            interactionMapper.removeLike(userId, mapId);
            result.put("liked", false);
        } else {
            // 未点赞，添加点赞
            interactionMapper.addLike(userId, mapId);
            result.put("liked", true);
        }

        // 更新地图点赞数
        interactionMapper.updateMapLikeCount(mapId);

        return result;
    }

    /**
     * 检查用户是否已点赞地图
     * @param userId 用户ID
     * @param mapId 地图ID
     * @return 是否已点赞
     */
    public boolean checkIsLiked(UUID userId, UUID mapId) {
        int exists = interactionMapper.checkLikeExists(userId, mapId);
        return exists > 0;
    }
}