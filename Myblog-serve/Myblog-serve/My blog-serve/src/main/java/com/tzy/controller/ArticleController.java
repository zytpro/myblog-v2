package com.tzy.controller;



import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tzy.config.Logger;
import com.tzy.pojo.Article;
import com.tzy.pojo.Result;
import com.tzy.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize) {
//        System.out.println("当前 Token：" + StpUtil.getTokenValue());
//        System.out.println("是否登录：" + StpUtil.isLogin());

        // 设置分页
        PageHelper.startPage(pageNum, pageSize);
        List<Article> articles = articleService.list();


        // 使用 PageInfo 包装结果
        PageInfo<Article> pageInfo = new PageInfo<>(articles);

        Map<String, Object> response = new HashMap<>();
        response.put("total", pageInfo.getTotal());
        response.put("articles", pageInfo.getList());
        return response;
    }

    // 获取文章详情接口
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticle(@PathVariable int id) {
        Article article = articleService.getArticleById(id);
        if (article != null) {
            return ResponseEntity.ok(article);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    //新增文章接口
    @PostMapping("/save")
    public Result<Article> saveArticle(@RequestBody Article article) {
        // 调用服务层保存文章
        Article savedArticle = articleService.saveArticle(article);

        // 返回保存后的文章数据
        if (savedArticle != null){
            return Result.success(savedArticle);
        }
        else {
            return Result.error("保存文章失败");
        }

    }

    //删除文章标签接口
    @GetMapping("/tags")
    public Result<List<String>> getTags() {
        List<String> tags = articleService.getTags();
        return Result.success(tags);
    }

    //获取文章分类接口
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<String> categories = articleService.getCategories();
        return Result.success(categories);
    }

    //删除文章接口
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable int id) {
        articleService.deleteArticle(id);
        return Result.success("删除成功");
    }

    //更新文章接口
    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable int id, @RequestBody Article article) {

        articleService.updateArticle(id, article);
        return Result.success(article);
    }

    //清理关系表垃圾数据
    @DeleteMapping("/clear")
    public Result<String> clear() {
        articleService.clear();
        return Result.success("清理成功");
    }


}
