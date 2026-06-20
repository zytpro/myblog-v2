package com.tzy.mapper;

import com.tzy.pojo.Article;
import com.tzy.pojo.ArticleTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    // 获取所有文章信息和对应标签,分类
    @Select("SELECT a.*, " +
            "GROUP_CONCAT(at.tags SEPARATOR ',') AS tags, " +  // 聚合标签，使用逗号分隔
            "c.category " +  // 获取分类名称
            "FROM article a " +
            "LEFT JOIN article_tags at ON a.id = at.article_id " +  // 通过 article_id 连接 article 和 article_tags
            "LEFT JOIN article_categories ac ON a.id = ac.id " +  // 连接 article_categories 表获取分类信息
            "LEFT JOIN article_categories c ON ac.id = c.id " +  // 获取分类名称
            "GROUP BY a.id")  // 按文章 ID 分组
    List<Article> list();


    // 获取单篇文章信息
    @Select("SELECT * FROM article WHERE id = #{id}")
    Article getArticleById(int id);

    // 保存文章
        @Insert("INSERT INTO article (title, description, views, comments, likes, image, content,pinned) " +
                "VALUES (#{title}, #{description}, #{views}, #{comments}, #{likes}, #{image}, #{content}, #{pinned})")
        @Options(useGeneratedKeys = true, keyProperty = "id") // 自动生成并填充主键 id
        int saveArticle(Article article);

        // 获取所有标签
    @Select("SELECT name FROM tags GROUP BY name")
    List<String> getTags();

    // 获取所有分类
    @Select("SELECT * FROM category")
    List<String> getCategories();

    // 保存文章标签
    @Insert("INSERT INTO article_tags (article_id, tags) VALUES (#{article_id}, #{tag})")
    void saveArticleTags(ArticleTag articleTag);

    @Insert("INSERT INTO article_categories (id, category) VALUES (#{articleId}, #{category})")
    void saveArticleCategory(@Param("articleId") Integer articleId, @Param("category") String category);

    @Delete("DELETE FROM article WHERE id = #{id}")
    void deleteArticle(int id);

    @Update("UPDATE article SET title = #{article.title}, description = #{article.description}, content = #{article.content}, pinned = #{article.pinned}, image = #{article.image} WHERE id = #{id}")
    void updateArticle(@Param("id") int id, @Param("article") Article article);


    @Select("SELECT name FROM category WHERE id = #{id}")
    Object getCategoryIdById(@Param("id") String categoryId);

    @Update("UPDATE article_tags SET tags = #{tags} WHERE article_id = #{id}")
    void updateArticleTags(@Param("id") String tagsId, @Param("tags") String tags);


    //更新文章分类关系表
    @Update("UPDATE article_categories set category = #{categoryName} WHERE id = #{id}")
    void updateArticleCategory(@Param("id") int id, @Param("categoryName") String categoryName);

    //清除多余数据
    @Delete("DELETE FROM article_categories WHERE id NOT IN (SELECT id FROM article)")
    void clear();
}
