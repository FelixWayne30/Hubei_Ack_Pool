package com.example.hubeiatlasbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.hubeiatlasbackend.service.MapInfoService;

@RestController
@CrossOrigin
public class MapInfoController extends BaseController {
    @Autowired
    private MapInfoService mapInfoService;

    @GetMapping("/mapinfo/getallmapinfo")
    public Object getAllMapController(){
        try {
            return renderSuccess(mapInfoService.getAllMapInfoList());
        }catch (Exception e){
            return renderError(e.getMessage());
        }

    }

}
