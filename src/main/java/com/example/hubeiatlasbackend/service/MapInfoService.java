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

    public List<Map<String, Objects>> getMaps() {
        return mapInfoMapper.getMaps();
    }

    public List<Map<String, Objects>> getMapsByGroupId(String group_id) { return mapInfoMapper.getMapsByGroupId(group_id); }

}
