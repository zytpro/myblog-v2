package com.tzy.service;

import com.tzy.pojo.Article;
import com.tzy.pojo.PageBean;
import com.tzy.pojo.Result;

import java.util.List;

public interface ArticleService {


    List<Article> list();

    Article getArticleById(int id);

    Article saveArticle(Article article);

    List<String> getTags();

    List<String> getCategories();


    void deleteArticle(int id);

    void updateArticle(int id, Article article);

    void clear();
}
