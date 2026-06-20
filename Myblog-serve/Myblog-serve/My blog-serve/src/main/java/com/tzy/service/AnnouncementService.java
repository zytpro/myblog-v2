package com.tzy.service;

import com.tzy.pojo.Announcement;
import java.util.List;

public interface AnnouncementService {
    List<Announcement> getAllAnnouncements();
    Announcement getAnnouncementById(Integer id);
    boolean addAnnouncement(Announcement announcement);
    boolean updateAnnouncement(Announcement announcement);
    boolean deleteAnnouncement(Integer id);
}
