package com.tzy.controller;

import com.tzy.pojo.HomeSettings;
import com.tzy.service.HomeSettingsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home-settings")
public class HomeSettingsController {

    @Resource
    private HomeSettingsService homeSettingsService;

    @GetMapping("/get")
    public Map<String, Object> getHomeSettings() {
        Map<String, Object> response = new HashMap<>();
        try {
            HomeSettings homeSettings = homeSettingsService.getHomeSettings();
            response.put("code", 0);
            response.put("data", homeSettings);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("code", 1);
            response.put("message", "获取失败: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public Map<String, Object> updateHomeSettings(@RequestBody HomeSettings homeSettings) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = homeSettingsService.updateHomeSettings(homeSettings);
            if (success) {
                response.put("code", 0);
                response.put("message", "更新成功");
            } else {
                response.put("code", 1);
                response.put("message", "更新失败");
            }
        } catch (Exception e) {
            response.put("code", 1);
            response.put("message", "更新失败: " + e.getMessage());
        }
        return response;
    }
}
