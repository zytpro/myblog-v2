package com.tzy.mapper;

import com.tzy.pojo.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 根据文章 ID 和状态获取评论
    @Select("SELECT * FROM comment WHERE article_id = #{articleId} AND status = #{status}")
    List<Comment> selectByArticleIdAndStatus(@Param("articleId") Long articleId, @Param("status") String status);

    // 根据文章 ID 获取所有评论
    @Select("SELECT * FROM comment WHERE article_id = #{articleId}")
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);

    // 插入评论
    @Insert("INSERT INTO comment (article_id, nickname, content, status) VALUES (#{articleId}, #{nickname}, #{content}, #{status})")
    void insertComment(Comment comment);

    // 删除评论
    @Delete("DELETE FROM comment WHERE id = #{id}")
    void deleteComment(@Param("id") Long id);
}
