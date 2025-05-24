package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.PublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@CrossOrigin
public class PublishController extends BaseController{
    @Autowired
    private PublishService PublishService;

    @PostMapping("/publish/uploadMapFile")
    public Object publishGeoTiff(
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") String type
    ){
        try {
            String map_id = PublishService.insertBaseMapInfo(file,type);
            return renderSuccess(PublishService.publishGeoTiff(file,map_id));
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/publish/delete")
    public Object deleteMap(
        @RequestParam("id") UUID id
    ){
        try {
            if(PublishService.deleteMapFile(id)){
                PublishService.deleteMapInfo(id);
                return renderSuccess("删除地图成功！");
            }
            else{
                return renderError("删除地图失败！");
            }
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

    @PostMapping("/publish/editMap")
    public Object editMap(
            @RequestParam("id") UUID id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file
    ){
        try {
            if (file != null && !file.isEmpty()) {
                PublishService.editMapFile(file,id);
                PublishService.deleteMapFile(id);
                PublishService.publishGeoTiff(file,id.toString());
            }
            PublishService.editMapInfo(id,name,description);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }
}
