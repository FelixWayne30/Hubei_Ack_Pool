package com.example.hubeiatlasbackend.service;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.hubeiatlasbackend.mapper.MapInfoMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class MapInfoService {

    @Resource
    private MapInfoMapper mapInfoMapper;

    public List<Map<String, Objects>> getAllMapInfoList() {
        return mapInfoMapper.getAllMapInfoList();
    }
}
