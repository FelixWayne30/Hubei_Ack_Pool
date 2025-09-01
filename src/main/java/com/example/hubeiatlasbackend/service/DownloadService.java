package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.DownloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class DownloadService {
    @Resource
    private DownloadMapper downloadMapper;

    public void addRequest(UUID mapId, UUID userId, String email, String reason) {
        downloadMapper.addRequest(mapId,userId,email,reason);
    }

    public List<Map<String, Object>> getRequests() {
        return downloadMapper.getRequests();
    }

    public void review(UUID requestID, Integer status) {
        downloadMapper.review(requestID,status);
    }

}
