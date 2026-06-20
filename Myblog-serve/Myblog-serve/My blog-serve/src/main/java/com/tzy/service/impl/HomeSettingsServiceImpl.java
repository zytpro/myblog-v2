package com.tzy.service.impl;

import com.tzy.mapper.HomeSettingsMapper;
import com.tzy.pojo.HomeSettings;
import com.tzy.service.HomeSettingsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HomeSettingsServiceImpl implements HomeSettingsService {

    @Resource
    private HomeSettingsMapper homeSettingsMapper;

    @Override
    public HomeSettings getHomeSettings() {
        HomeSettings homeSettings = homeSettingsMapper.getHomeSettings();
        if (homeSettings == null) {
            // 如果没有数据，创建默认数据
            homeSettings = new HomeSettings();
            homeSettings.setWebTitle("我的博客");
            homeSettings.setBackgroundImage("/img/back1.png");
            homeSettings.setTypewriterTexts("你看对面的青山多漂亮,春风得意马蹄疾，一日看尽长安花,山重水复疑无路，柳暗花明又一村,会当凌绝顶，一览众山小,海上生明月，天涯共此时,落红不是无情物，化作春泥更护花,长风破浪会有时，直挂云帆济沧海,天生我材必有用，千金散尽还复来");
            homeSettings.setFooterQuote("云想衣裳花想容,春风拂槛露华浓。");
            homeSettings.setIcpLicense("滇ICP备2025053841号-1");
            homeSettings.setContactEmail("1363269065@qq.com");
            homeSettingsMapper.insertHomeSettings(homeSettings);
        } else {
            // 如果有数据，但新字段为null，设置默认值
            if (homeSettings.getFooterQuote() == null) {
                homeSettings.setFooterQuote("云想衣裳花想容,春风拂槛露华浓。");
            }
            if (homeSettings.getIcpLicense() == null) {
                homeSettings.setIcpLicense("滇ICP备2025053841号-1");
            }
            if (homeSettings.getContactEmail() == null) {
                homeSettings.setContactEmail("1363269065@qq.com");
            }
        }
        return homeSettings;
    }

    @Override
    public boolean updateHomeSettings(HomeSettings homeSettings) {
        // 先检查是否存在数据
        HomeSettings existingSettings = homeSettingsMapper.getHomeSettings();
        if (existingSettings == null) {
            // 如果没有数据，先插入
            int insertResult = homeSettingsMapper.insertHomeSettings(homeSettings);
            return insertResult > 0;
        } else {
            // 如果有数据，更新
            // 设置id为现有数据的id
            homeSettings.setId(existingSettings.getId());
            int updateResult = homeSettingsMapper.updateHomeSettings(homeSettings);
            return updateResult > 0;
        }
    }
}
