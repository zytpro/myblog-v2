package com.tzy.controller;


import com.tzy.pojo.Tags;
import com.tzy.pojo.Result;
import com.tzy.service.TagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagsController {

    @Autowired
    private TagsService TagsService;

    // 查询所有标签
    @GetMapping
    public List<Tags> getAllTagsies() {
        return TagsService.list();
    }

    // 根据 ID 查询标签
    @GetMapping("/{id}")
    public Tags getTagsyById(@PathVariable Integer id) {
        return TagsService.getById(id);
    }

    // 新增标签
    @PostMapping
    public Result createTagsy(@RequestBody Tags Tags) {
        Boolean result =  TagsService.save(Tags);
        if (result) {
            return Result.success("新增标签成功");
        } else {
            return Result.error("新增标签失败");
        }
    }

    // 更新标签
    @PutMapping
    public Result updateTagsy(@RequestBody Tags Tags) {
        Boolean result =  TagsService.updateById(Tags);
        if (result) {
            return Result.success("更新标签成功");
        }
        else {
            return Result.error("更新标签失败");
        }
    }

    // 删除标签
    @DeleteMapping("/{id}")
    public Result deleteTagsy(@PathVariable Integer id) {
        Boolean result =  TagsService.removeById(id);
        if (result) {
            return Result.success("更新标签成功");
        }
        else {
            return Result.error("更新标签失败");
        }
    }
}
