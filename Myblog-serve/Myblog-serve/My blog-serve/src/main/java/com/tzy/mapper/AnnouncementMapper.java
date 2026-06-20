package com.tzy.mapper;

import com.tzy.pojo.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    // 获取所有公告
    @Select("SELECT * FROM announcement")
    List<Announcement> selectAll();

    // 根据ID获取公告
    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement selectById(Integer id);

    // 添加公告
    @Insert("INSERT INTO announcement (content, create_time, status) VALUES (#{content}, #{createTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    // 更新公告
    @Update("UPDATE announcement SET content = #{content}, status = #{status}, create_time = #{createTime} WHERE id = #{id}")
    int update(Announcement announcement);

    // 删除公告
    @Delete("DELETE  FROM announcement WHERE id = #{id}")
    int delete(Integer id);
}
