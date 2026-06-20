package com.tzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tzy.pojo.Category;
import com.tzy.pojo.Result;
import com.tzy.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 查询所有分类
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.list();
    }

    // 根据 ID 查询分类
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Integer id) {
        return categoryService.getById(id);
    }

    // 新增分类
    @PostMapping
    public Result createCategory(@RequestBody Category category) {
        //检索是否存在同名分类
        List<Category> categories = categoryService.list(new QueryWrapper<Category>().eq("name", category.getName()));
        if (categories.size() > 0) {
            return Result.error("分类已存在");
        }
        categoryService.save(category);
        return Result.success();
    }

    // 更新分类
    @PutMapping
    public Result updateCategory(@RequestBody Category category) {
        Boolean result =  categoryService.updateById(category);
        if (result) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    // 删除分类
    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        Boolean result =  categoryService.removeById(id);
        if (result) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}

