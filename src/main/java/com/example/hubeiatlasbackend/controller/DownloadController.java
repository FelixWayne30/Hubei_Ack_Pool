package com.example.hubeiatlasbackend.controller;


import com.example.hubeiatlasbackend.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/download")
public class DownloadController extends BaseController{
    @Autowired
    private DownloadService downloadService;

    @GetMapping("/addRequest")
    public Object addRequest(
            @RequestParam("map_id") UUID mapId,
            @RequestParam("user_id") UUID userId,
            @RequestParam("email") String email,
            @RequestParam("reason") String reason
    ){
        try{
            downloadService.addRequest(mapId,userId,email,reason);
            return renderSuccess();
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }

//    @GetMapping("/requests")
//    public Object getRequests(){}
}
