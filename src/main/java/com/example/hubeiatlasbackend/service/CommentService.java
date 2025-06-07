package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class CommentService {
    @Resource
    private CommentMapper commentMapper;

    public void addComment(UUID userId, UUID mapId, String comment){
        commentMapper.addComment(userId,mapId,comment);
    }

    public List<Map<String, Objects>> getComments() {
        return commentMapper.getComments();
    }

    public void review(UUID commentId, Integer status) {
        commentMapper.review(commentId,status);
    }
}
