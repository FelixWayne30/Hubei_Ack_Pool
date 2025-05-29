package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.CustomListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/customlist")
public class CustomListController extends BaseController {

    @Autowired
    private CustomListService customListService;

    /**
     * 获取用户的所有自定义列表
     */
    @GetMapping("/list")
    public Object getUserLists(@RequestParam("userId") String userIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            List<Map<String, Object>> lists = customListService.getUserLists(userId);
            return renderSuccess(lists);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 获取自定义列表详情
     */
    @GetMapping("/detail/{listId}")
    public Object getListDetail(@PathVariable("listId") String listIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            Map<String, Object> listDetail = customListService.getListDetail(listId);
            if (listDetail == null) {
                return renderError("列表不存在");
            }
            return renderSuccess(listDetail);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 创建自定义列表
     */
    @PostMapping("/create")
    public Object createList(@RequestParam("userId") String userIdStr,
                             @RequestParam("name") String name,
                             @RequestParam(value = "description", required = false) String description) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID listId = customListService.createList(userId, name, description);
            return renderSuccess(Map.of("listId", listId));
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 更新自定义列表
     */
    @PostMapping("/update")
    public Object updateList(@RequestParam("listId") String listIdStr,
                             @RequestParam("userId") String userIdStr,
                             @RequestParam("name") String name,
                             @RequestParam(value = "description", required = false) String description) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID userId = UUID.fromString(userIdStr);
            boolean success = customListService.updateList(listId, userId, name, description);
            if (!success) {
                return renderError("更新失败，请检查列表ID和用户ID是否正确");
            }
            return renderSuccess("更新成功");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 删除自定义列表
     */
    @PostMapping("/delete")
    public Object deleteList(@RequestParam("listId") String listIdStr,
                             @RequestParam("userId") String userIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID userId = UUID.fromString(userIdStr);
            boolean success = customListService.deleteList(listId, userId);
            if (!success) {
                return renderError("删除失败，请检查列表ID和用户ID是否正确");
            }
            return renderSuccess("删除成功");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 添加地图到列表
     */
    @PostMapping("/addMap")
    public Object addMapToList(@RequestParam("listId") String listIdStr,
                               @RequestParam("mapId") String mapIdStr,
                               @RequestParam("userId") String userIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            UUID userId = UUID.fromString(userIdStr);
            boolean success = customListService.addMapToList(listId, mapId, userId);
            if (!success) {
                return renderError("添加失败，请检查列表ID和用户ID是否正确");
            }
            return renderSuccess("添加成功");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 批量添加地图到列表
     */
    @PostMapping("/addMaps")
    public Object addMapsToList(@RequestParam("listId") String listIdStr,
                                @RequestParam("mapIds") List<String> mapIdStrs,
                                @RequestParam("userId") String userIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID userId = UUID.fromString(userIdStr);

            List<UUID> mapIds = new ArrayList<>();
            for (String mapIdStr : mapIdStrs) {
                mapIds.add(UUID.fromString(mapIdStr));
            }

            boolean success = customListService.addMapsToList(listId, mapIds, userId);
            if (!success) {
                return renderError("添加失败，请检查列表ID和用户ID是否正确");
            }
            return renderSuccess("添加成功");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 从列表中移除地图
     */
    @PostMapping("/removeMap")
    public Object removeMapFromList(@RequestParam("listId") String listIdStr,
                                    @RequestParam("mapId") String mapIdStr,
                                    @RequestParam("userId") String userIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            UUID userId = UUID.fromString(userIdStr);
            boolean success = customListService.removeMapFromList(listId, mapId, userId);
            if (!success) {
                return renderError("移除失败，请检查列表ID和用户ID是否正确");
            }
            return renderSuccess("移除成功");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 批量从列表中移除地图
     */
    @PostMapping("/removeMaps")
    public Object removeMapsFromList(@RequestParam("listId") String listIdStr,
                                     @RequestParam("mapIds") List<String> mapIdStrs,
                                     @RequestParam("userId") String userIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID userId = UUID.fromString(userIdStr);

            List<UUID> mapIds = new ArrayList<>();
            for (String mapIdStr : mapIdStrs) {
                mapIds.add(UUID.fromString(mapIdStr));
            }

            boolean success = customListService.removeMapsFromList(listId, mapIds, userId);
            if (!success) {
                return renderError("移除失败，请检查列表ID和用户ID是否正确");
            }
            return renderSuccess("移除成功");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    /**
     * 检查地图是否在列表中
     */
    @GetMapping("/checkMap")
    public Object checkMapInList(@RequestParam("listId") String listIdStr,
                                 @RequestParam("mapId") String mapIdStr) {
        try {
            UUID listId = UUID.fromString(listIdStr);
            UUID mapId = UUID.fromString(mapIdStr);
            boolean inList = customListService.isMapInList(listId, mapId);
            return renderSuccess(Map.of("inList", inList));
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }
}