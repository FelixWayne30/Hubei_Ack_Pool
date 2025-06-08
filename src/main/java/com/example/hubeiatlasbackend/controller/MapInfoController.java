package com.example.hubeiatlasbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.hubeiatlasbackend.service.MapInfoService;
import org.springframework.web.multipart.MultipartFile;

import java.awt.font.MultipleMaster;
import java.util.UUID;

@RestController
@CrossOrigin
public class MapInfoController extends BaseController {
    @Autowired
    private MapInfoService mapInfoService;

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

    @GetMapping("/mapinfo/maps/{groupid}")
    public Object getMapsByGroupId(@PathVariable("groupid") UUID groupId) {
        try {
            return renderSuccess(mapInfoService.getMapsByGroupId(groupId));
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

    @GetMapping("/mapinfo/search")
    public Object searchMaps(@RequestParam("query") String query,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            return renderSuccess(mapInfoService.searchMaps(query, page, size));
        } catch (Exception e) {
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

}
