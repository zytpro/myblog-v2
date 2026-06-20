package com.tzy.controller;

import com.tzy.pojo.FreeInterface;
import com.tzy.pojo.Result;
import com.tzy.service.ApiService;
import com.tzy.utils.Spider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping
    public Result<List> getApi(){
        List<FreeInterface> freeInterfaces = apiService.getApiList();

        System.out.println(freeInterfaces);
        return Result.success(freeInterfaces);
    }

    /**
     * 增加热搜名称和链接
     * @param name 热搜名称
     * @param link 热搜链接
     * @return Result
     */
     @PostMapping("addlink")
    public Result addLink(@RequestParam String name, @RequestParam String link){
         //校验参数
         if (name == null || name.trim().isEmpty() || link == null || link.trim().isEmpty()) {
             return Result.error("热搜名称或链接不能为空");
         }
          else if (apiService.addLink(name, link)){
              return Result.success();
          }
          return Result.error("热搜名称或链接已存在");
     }

    /**
     * 删除热搜名称和链接
     * @param id 热搜id
     * @return Result
     */
    @DeleteMapping("deletelink")
    public Result deleteLink(@RequestParam String id){
        //校验参数
        if (id == null || id.trim().isEmpty()) {
            return Result.error("热搜id不能为空");
        }
        else if (apiService.deleteLink(id)){
            return Result.success();
        }
        return Result.error("热搜不存在");
    }

    /**
     * 调用爬虫获取热搜数据
     */
    @GetMapping("getHotSearch")
    public Result<String> getHotSearch(@RequestParam String url) {
        // 调用爬虫方法获取数据
        String response = Spider.crawl(url);

        // 判断是否爬取成功
        if (response.startsWith("Error")) {
            // 如果爬取失败，返回失败的 Result
            return Result.error(response);
        } else {
            // 如果爬取成功，返回成功的 Result
            return Result.success(response);
        }
    }

}
