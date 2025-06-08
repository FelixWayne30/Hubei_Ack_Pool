package com.example.hubeiatlasbackend.mapper;

import java.util.UUID;

public interface DownloadMapper {

    void addRequest(UUID mapId, UUID userId, String email, String reason);
}
