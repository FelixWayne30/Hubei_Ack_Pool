package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.StyleTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/styletransfer")
public class StyleTransferController extends BaseController {

    @Autowired
    private StyleTransferService styleTransferService;

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
}