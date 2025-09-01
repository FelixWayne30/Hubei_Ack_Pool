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
            // 在这里添加日志打印，以便查看具体的异常信息
            e.printStackTrace();
            System.out.println("下载请求处理失败: " + e.getMessage());
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/requests")
    public Object getRequests() {
        try {
            Object requests = downloadService.getRequests();
            return renderSuccess(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return renderError(e.getMessage());
        }
    }

    @PostMapping("/review")
    public Object review(@RequestParam("id") UUID request_id,
                         @RequestParam("status") Integer status) {
        try {
            downloadService.review(request_id,status);
            return renderSuccess();
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

}
