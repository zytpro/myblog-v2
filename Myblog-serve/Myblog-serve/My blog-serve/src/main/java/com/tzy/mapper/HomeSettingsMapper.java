package com.tzy.mapper;

import com.tzy.pojo.HomeSettings;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface HomeSettingsMapper {
    @Select("SELECT * FROM home_settings LIMIT 1")
    HomeSettings getHomeSettings();
    
    @Update("UPDATE home_settings SET background_image = #{backgroundImage}, typewriter_texts = #{typewriterTexts}, web_title = #{webTitle}, footer_quote = #{footerQuote}, icp_license = #{icpLicense}, contact_email = #{contactEmail} WHERE id = #{id}")
    int updateHomeSettings(HomeSettings homeSettings);
    
    @Insert("INSERT INTO home_settings (background_image, typewriter_texts, web_title, footer_quote, icp_license, contact_email) VALUES (#{backgroundImage}, #{typewriterTexts}, #{webTitle}, #{footerQuote}, #{icpLicense}, #{contactEmail})")
    int insertHomeSettings(HomeSettings homeSettings);
}
