package com.example.hubeiatlasbackend.service;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.example.hubeiatlasbackend.mapper.MapInfoMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class MapInfoService {

    @Resource
    private MapInfoMapper mapInfoMapper;

    public List<Map<String, Objects>> getTopics() {
        return mapInfoMapper.getTopics();
    }

    public List<Map<String, Objects>> getMapsByGroupId(String group_id) { return mapInfoMapper.getMapsByGroupId(group_id); }

    public String uploadMap(MultipartFile file) {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("tmp", ".jpg");
            file.transferTo(tmpFile);
        } catch (IOException e) {
            return "读取文件异常：" + e.toString();
        }


        File outputDir = new File("tiles_output");
        if (!outputDir.exists()) outputDir.mkdirs();

        ProcessBuilder builder = new ProcessBuilder(
                "src/main/resources/static/transfer2tif.bat",
                tmpFile.getAbsolutePath().substring(0, tmpFile.getAbsolutePath().length() - 4),
                outputDir.getAbsolutePath()+"output"
        );

        System.out.println(tmpFile.getAbsolutePath().substring(0, tmpFile.getAbsolutePath().length() - 4)+" "+outputDir.getAbsolutePath()+"\\output");

        Process process;
        try{
            process = builder.inheritIO().start();
        }catch(IOException e){
            return "转换异常：" + e.toString();
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "切片失败"+ String.valueOf(exitCode);
            }
        } catch (InterruptedException e) {
            return "切片异常:" + e.toString();
        }

        return outputDir.toString();

    }
}
