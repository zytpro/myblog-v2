package com.tzy.service;

import com.tzy.pojo.HomeSettings;

public interface HomeSettingsService {
    HomeSettings getHomeSettings();
    boolean updateHomeSettings(HomeSettings homeSettings);
}
