package com.example.hubeiatlasbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.hubeiatlasbackend.service.MapInfoService;
import com.example.hubeiatlasbackend.service.SmartSearchService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
public class MapInfoController extends BaseController {

    @Autowired
    private MapInfoService mapInfoService;

    @Autowired
    private SmartSearchService smartSearchService;


    @GetMapping("/mapinfo/topics")
    public Object getTopicsController(){
        try {
            return renderSuccess(mapInfoService.getTopics());
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/maps")
    public Object getMapsController(){
        try {
            return renderSuccess(mapInfoService.getMaps());
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/map/{map_id}")
    public Object getMapsByMapIdController(@PathVariable("map_id") UUID mapId){
        try {
            return renderSuccess(mapInfoService.getMapsByMapId(mapId));
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/getMapsByTopic")
    public Object getMapsByGroupId(
            @RequestParam(name="groupid",required = false) UUID groupId,
            @RequestParam(name="group",required = false) String group
            ) {
        try {
            if(groupId != null && !groupId.equals(new UUID(0L, 0L))){
                return renderSuccess(mapInfoService.getMapsByGroupId(groupId));
            }
            else if(group != null && !group.trim().isEmpty()){
                return renderSuccess(mapInfoService.getMapsByGroupName(group));
            }
            else{
                return renderError(HttpStatus.BAD_REQUEST.toString());
            }
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/bannerMaps")
    public Object getBannerMapsController(){
        try {
            return renderSuccess(mapInfoService.getBannerMaps());
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/updateMapOrder")
    public Object updateMapOrder(
            @RequestParam("topicId") UUID topicId,
            @RequestParam("mapId") UUID mapId,
            @RequestParam("map_rank") int map_rank
    ) {
        try {
            mapInfoService.updateMapOrder(topicId,mapId,map_rank);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/addMaptoTopic")
    public Object addMaptoTopic(
            @RequestParam("topic_id") UUID topicId,
            @RequestParam("map_id") UUID mapId,
            @RequestParam("map_rank") int map_rank
    ) {
        try {
            mapInfoService.addMaptoTopic(topicId,mapId,map_rank);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/removeMapfromTopic")
    public Object removeMapfromTopic(
            @RequestParam("topic_id") UUID topicId,
            @RequestParam("map_id") UUID mapId,
            @RequestParam("map_rank") int map_rank
    ) {
        try {
            mapInfoService.removeMapfromTopic(topicId,mapId,map_rank);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/removeAllMapsfromTopic")
    public Object removeAllMapsfromTopic(
            @RequestParam("topic_id") UUID topicId
    ) {
        try {
            mapInfoService.removeAllMapsfromTopic(topicId);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/addTopic")
    public Object addTopic(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("order") int order
    ) {
        try {
            mapInfoService.addTopic(name,description,order);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/deleteTopic")
    public Object deleteTopic(
            @RequestParam("topic_id") UUID topic_id
    ) {
        try {
            mapInfoService.deleteTopic(topic_id);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/updateGroupOrder")
    public Object updateGroupOrder(
            @RequestParam("topic_id") UUID topic_id,
            @RequestParam("order") int order
    ) {
        try {
            mapInfoService.updateGroupOrder(topic_id,order);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/editGroupInfo")
    public Object editGroupInfo(
            @RequestParam("id") UUID topic_id,
            @RequestParam("name") String name,
            @RequestParam("description") String description
    ) {
        try {
            mapInfoService.editGroupInfo(topic_id,name,description);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/updateBannerMapOrder")
    public Object updateBannerMapOrder(
            @RequestParam("mapId") UUID mapId,
            @RequestParam("map_rank") int map_rank
    ) {
        try {
            mapInfoService.updateBannerMapOrder(mapId,map_rank);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/addMaptoBanner")
    public Object addMaptoBanner(
            @RequestParam("map_id") UUID mapId,
            @RequestParam("map_rank") int map_rank
    ) {
        try {
            mapInfoService.addMaptoBanner(mapId,map_rank);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/removeMapfromBanner")
    public Object removeMapfromBanner(
            @RequestParam("map_id") UUID mapId,
            @RequestParam("map_rank") int map_rank
    ) {
        try {
            mapInfoService.removeMapfromBanner(mapId,map_rank);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/getTopicByMapId/{mapId}")
    public Object getTopicByMapId(@PathVariable("mapId") String mapIdStr) {
        try {
            UUID mapId = UUID.fromString(mapIdStr);
            List<Map<String, Object>> topicInfo = mapInfoService.getTopicByMapId(mapId);

            if (topicInfo.isEmpty()) {
                return renderSuccess("该地图暂无专题信息", null);
            }

            return renderSuccess(topicInfo.get(0));
        } catch (IllegalArgumentException e) {
            return renderError("地图ID格式错误");
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/mapinfo/search")
    public Object aiSearch(@RequestParam("query") String query,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Map<String, Object> result = smartSearchService.aiSearch(query, page, size);
            return renderSuccess("智能搜索完成", result);
        } catch (Exception e) {
            return renderError("智能搜索失败: " + e.getMessage());
        }
    }
}