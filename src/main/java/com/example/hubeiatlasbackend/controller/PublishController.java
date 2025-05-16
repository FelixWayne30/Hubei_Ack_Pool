package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.PublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class PublishController extends BaseController{
    @Autowired
    private PublishService PublishService;

    @PostMapping("/publish-geotiff")
    public Object publishGeoTiff(
            @RequestParam("file") MultipartFile file,
            @RequestParam("map_id") String map_id
    ){
        try {
            return renderSuccess(PublishService.publishGeoTiff(file,map_id));
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }
}
