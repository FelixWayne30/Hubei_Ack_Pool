package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.PublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
}
