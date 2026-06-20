package com.tzy.controller;

import com.tzy.pojo.Announcement;
import com.tzy.service.AnnouncementService;
import com.tzy.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // 获取所有公告
    @GetMapping("/list")
    public Result<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return Result.success(announcements);
    }

    // 根据ID获取公告
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Integer id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    // 添加公告
    @PostMapping("/add")
    public Result addAnnouncement(@RequestBody Announcement announcement) {
        boolean isAdded = announcementService.addAnnouncement(announcement);
        if (isAdded) {
            return Result.success();
        }
        return Result.error("公告添加失败");
    }

    // 更新公告
    @PutMapping("/update")
    public Result updateAnnouncement(@RequestBody Announcement announcement) {
        boolean isUpdated = announcementService.updateAnnouncement(announcement);
        if (isUpdated) {
            return Result.success("公告更新成功");
        }
        return Result.error("公告更新失败");
    }

    // 删除公告
    @DeleteMapping("/delete/{id}")
    public Result deleteAnnouncement(@PathVariable Integer id) {
        boolean isDeleted = announcementService.deleteAnnouncement(id);
        if (isDeleted) {
            return Result.success("公告删除成功");
        }
        return Result.error("公告删除失败");
    }
}
