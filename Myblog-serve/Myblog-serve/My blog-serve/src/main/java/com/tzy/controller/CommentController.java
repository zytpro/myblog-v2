package com.tzy.controller;

import com.tzy.pojo.Comment;
import com.tzy.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 获取正常评论
    @GetMapping("/{articleId}")
    public List<Comment> getComments(@PathVariable Long articleId) {
        return commentService.getCommentsByArticleId(articleId);
    }

    // 获取所有评论（包括待审核、已删除）
    @GetMapping("/all/{articleId}")
    public List<Comment> getAllComments(@PathVariable Long articleId) {
        return commentService.getAllCommentsByArticleId(articleId);
    }

    // 添加评论
    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        // 检查评论内容长度
        if (comment.getContent().length() > 500) {
            return ResponseEntity.badRequest().body("评论内容过长");
        }
        // 设置 IP 地址作为昵称
        commentService.addComment(comment, request);
        return ResponseEntity.ok("评论成功");
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("评论已删除");
    }
}
