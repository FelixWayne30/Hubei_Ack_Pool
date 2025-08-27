package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.StyleTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/styletransfer")
public class StyleTransferController extends BaseController {

    @Autowired
    private StyleTransferService styleTransferService;

    @Value("${pictures.location}")
    private String pictureLoc;

    /**
     * 生成调色板
     */
    @PostMapping("/palette")
    public Object generatePalette(@RequestBody Map<String, String> request) {
        try {
            String styleText = request.get("styleText");
            if (styleText == null || styleText.trim().isEmpty()) {
                return renderError("请输入风格描述");
            }

            Map<String, Object> result = styleTransferService.generatePalette(styleText);
            return renderSuccess("成功", result);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    @PostMapping("/transform")
    public Object generateTransfromImage(@RequestBody Map<String, String> request) {
        try {
            String imageFileName = request.get("imageFileName");
            if (imageFileName == null || imageFileName.trim().isEmpty()) {
                return renderError("请输入图片名称");
            }
            String styleText = request.get("styleText");
            if (styleText == null || styleText.trim().isEmpty()) {
                return renderError("请输入风格描述");
            }

            File file = new File(pictureLoc,imageFileName+".jpg");
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = styleTransferService.generatePalette(styleText);

            //色彩转换逻辑
            byte[] bytes = styleTransferService.applyStyle(file, (List<int[]>) result.get("colors"));
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return ResponseEntity.ok("data:image/png;base64," + base64);
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }
}