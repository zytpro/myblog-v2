package com.tzy.service.impl;

import com.tzy.mapper.AnnouncementMapper;
import com.tzy.pojo.Announcement;
import com.tzy.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.selectAll();
    }

    @Override
    public Announcement getAnnouncementById(Integer id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public boolean addAnnouncement(Announcement announcement) {
        return announcementMapper.insert(announcement) > 0;
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        return announcementMapper.update(announcement) > 0;
    }

    @Override
    public boolean deleteAnnouncement(Integer id) {
        return announcementMapper.delete(id) > 0;
    }
}
