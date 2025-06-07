package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/comment")
public class CommentController extends BaseController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/addcomment")
    public Object addComment(@RequestParam("userId") UUID userId,
                                   @RequestParam("mapId") UUID mapId,
                                   @RequestParam("comment") String comment) {
        try {
            commentService.addComment(userId, mapId,comment);
            return renderSuccess();
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    @GetMapping("/comments")
    public Object getComments() {
        try {
            return renderSuccess(commentService.getComments());
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }

    @PostMapping("/review")
    public Object review(@RequestParam("id") UUID comment_id,
                         @RequestParam("status") Integer status) {
        try {
            commentService.review(comment_id,status);
            return renderSuccess();
        } catch (Exception e) {
            return renderError(e.getMessage());
        }
    }
}
