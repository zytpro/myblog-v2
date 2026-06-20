package com.tzy.service;

import com.tzy.mapper.CommentMapper;
import com.tzy.pojo.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    // 获取正常评论
    public List<Comment> getCommentsByArticleId(Long articleId) {
        return commentMapper.selectByArticleIdAndStatus(articleId, "approved");
    }

    // 获取所有评论（包括待审核、已删除）
    public List<Comment> getAllCommentsByArticleId(Long articleId) {
        return commentMapper.selectByArticleId(articleId);
    }

    // 添加评论
    public void addComment(Comment comment, HttpServletRequest request) {
        // 获取客户端的 IP 地址
        String ipAddress = getClientIp(request);
        comment.setNickname(ipAddress); // 将 IP 地址作为昵称

        // 设置评论状态为已批准
        comment.setStatus(1);

        // 保存评论
        commentMapper.insertComment(comment);
    }


    // 删除评论
    public void deleteComment(Long id) {
        commentMapper.deleteComment(id);
    }

    // 获取请求的 IP 地址
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
