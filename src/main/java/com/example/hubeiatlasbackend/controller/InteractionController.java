package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/interaction")
public class InteractionController extends BaseController {

    @Autowired
    private InteractionService interactionService;

    /**
     * 切换收藏状态
     */
    @PostMapping("/collection/toggle")
    public Object toggleCollection(@RequestParam("userId") String userIdStr,
                                   @RequestParam("mapId") String mapIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            Map<String, Object> result = interactionService.toggleCollection(userId, mapId);
            return renderSuccess(result);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 获取用户的收藏列表
     */
    @GetMapping("/collection/list")
    public Object getUserCollections(@RequestParam("userId") String userIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            return renderSuccess(interactionService.getUserCollections(userId));
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 检查用户是否已收藏地图
     */
    @GetMapping("/collection/check")
    public Object checkIsCollected(@RequestParam("userId") String userIdStr,
                                   @RequestParam("mapId") String mapIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            boolean isCollected = interactionService.checkIsCollected(userId, mapId);
            return renderSuccess(Map.of("collected", isCollected));
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 切换点赞状态
     */
    @PostMapping("/like/toggle")
    public Object toggleLike(@RequestParam("userId") String userIdStr,
                             @RequestParam("mapId") String mapIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            Map<String, Object> result = interactionService.toggleLike(userId, mapId);
            return renderSuccess(result);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 检查用户是否已点赞地图
     */
    @GetMapping("/like/check")
    public Object checkIsLiked(@RequestParam("userId") String userIdStr,
                               @RequestParam("mapId") String mapIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            boolean isLiked = interactionService.checkIsLiked(userId, mapId);
            return renderSuccess(Map.of("liked", isLiked));
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }
}