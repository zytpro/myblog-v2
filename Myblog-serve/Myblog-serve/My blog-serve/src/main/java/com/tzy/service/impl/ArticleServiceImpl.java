package com.tzy.service.impl;

import com.tzy.mapper.ArticleMapper;
import com.tzy.pojo.Article;
import com.tzy.pojo.ArticleTag;
import com.tzy.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<Article> list() {
        return articleMapper.list();
    }

    @Override
    public Article getArticleById(int id) {
        return articleMapper.getArticleById(id);
    }

    @Override
    public Article saveArticle(Article article) {
        // 设置默认值
        article.setViews(article.getViews() != null ? article.getViews() : "0");
        article.setLikes(article.getLikes() != null ? article.getLikes() : "0");
        article.setComments(article.getComments() != null ? article.getComments() : "0");
        article.setImage(article.getImage() == null ? "" : article.getImage());
        article.setPinned(article.getPinned() != null ? article.getPinned() : "0");


        // 获取文章分类对应的名称 例如 1 -> 前端
          String categoryId = article.getCategory();
         String category = articleMapper.getCategoryIdById(categoryId).toString();
//        article.setCategory(categoryId);

        // 调用 Mapper 保存文章
        int rows = articleMapper.saveArticle(article);
        if (rows > 0) {
            // 获取保存的文章ID
            Integer articleId = article.getId();
            System.out.println("保存文章成功，文章ID：" + articleId);

            // 保存标签到 article_tag 表
            String tags = article.getTags().toString();
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticle_id(articleId);
            articleTag.setTag(tags);
            articleMapper.saveArticleTags(articleTag);

            // 保存分类等
            articleMapper.saveArticleCategory(articleId, category);
            return article;
        } else {
            throw new RuntimeException("保存文章失败");
        }
    }



    @Override
    public List<String> getTags() {
        return articleMapper.getTags();
    }

    @Override
    public List<String> getCategories() {
        return articleMapper.getCategories();
    }

    @Override
    public void deleteArticle(int id) {
        articleMapper.deleteArticle(id);
    }

    @Override
    public void updateArticle(int id, Article article) {
        //查询分类名称
        String categoryName = articleMapper.getCategoryIdById(article.getCategory()).toString();
        //构建更新对象
        Article updateArticle = new Article();
        updateArticle.setTitle(article.getTitle());
        updateArticle.setDescription(article.getDescription());
        updateArticle.setContent(article.getContent());
        updateArticle.setPinned(article.getPinned());
        updateArticle.setImage(article.getImage()); // 添加图片字段
        updateArticle.setCategory(article.getCategory());
        String tagsId = article.getId().toString();
        System.out.println("tagsId:" + tagsId);
        String tags = article.getTags();
        System.out.println("tags:" + tags);
        System.out.println("更新文章图片:" + article.getImage()); // 添加调试日志
        articleMapper.updateArticleTags(tagsId, tags);
        articleMapper.updateArticle(id, updateArticle);

        //更新分类关系表
        articleMapper.updateArticleCategory(id, categoryName);

    }

    @Override
    public void clear() {
        articleMapper.clear();
    }
 
    // 每天凌晨 3 点执行清理任务
    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = "*/10 * * * * ?")
    public void scheduledClear() {
        clear();
        System.out.println("定时任务执行：清理无效的文章关系数据");
    }


}
