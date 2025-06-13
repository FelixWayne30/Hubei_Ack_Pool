package com.example.hubeiatlasbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/image")
public class PictureController extends BaseController{
    @Value("${pictures.location}")
    private String pictureLoc;

    @GetMapping("/{filename}")
    public Object getPicture(@PathVariable("filename")String filename) {
        try {
            File file = new File(pictureLoc,filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = Files.readAllBytes(file.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }catch (Exception e){
            return renderError(e.getMessage());
        }
    }
}
