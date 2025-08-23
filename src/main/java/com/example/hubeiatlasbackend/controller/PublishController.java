package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.PublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin
public class PublishController extends BaseController{
    @Autowired
    private PublishService PublishService;

    @PostMapping("/publish/uploadMapFile")
    public Object publishMap(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "originTopic", required = false) String originTopic,
            @RequestParam(value = "subTopic", required = false) String subTopic,
            @RequestParam(value = "subitem", required = false) String subitem,
            @RequestParam(value = "parentMapName", required = false) String parentMapName
    ) {
        try {
            // 将前端上传的额外参数统一封装成 Map 或 DTO，传给 Service
            Map<String, String> params = new HashMap<>();
            params.put("originTopic", originTopic);
            params.put("subTopic", subTopic);
            params.put("subitem", subitem);
            params.put("parentMapName", parentMapName);

            PublishService.insertBaseMapInfo(file, type, params);

            return renderSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/publish/delete")
    public Object deleteMap(
        @RequestParam("id") UUID id,
        @RequestParam("name") String name
    ){
        try {
            PublishService.deleteMapFile(id,name);
            return renderSuccess("删除地图成功！");
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
                PublishService.deleteMapFile(id,name);
                //PublishService.insertBaseMapInfo(file,id.toString());
            }
            PublishService.editMapInfo(id,name,description);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }
}
