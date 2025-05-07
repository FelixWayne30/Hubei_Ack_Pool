package com.example.hubeiatlasbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.hubeiatlasbackend.service.MapInfoService;
import org.springframework.web.multipart.MultipartFile;

import java.awt.font.MultipleMaster;

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

    @GetMapping("/mapinfo/maps/{groupid}")
    public Object getMapsByGroupId(@PathVariable("groupid") String groupId) {
        try {
            return renderSuccess(mapInfoService.getMapsByGroupId(groupId));
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @PostMapping("/upload/map")
    public Object uploadMap(@RequestParam("file")MultipartFile file){
        try {
            return renderSuccess(mapInfoService.uploadMap(file));
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

}
