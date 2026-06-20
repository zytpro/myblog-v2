package com.tzy.mapper;

import com.tzy.pojo.LoveTime;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 恋爱时光数据访问接口
 *
 * @author tzy
 * @date 2024/04/05
 */
@Mapper
public interface LoveTimeMapper {

    /**
     * 添加一条恋爱时光记录
     */
    @Insert("INSERT INTO love_time(id, creat_time, content, creat_people) VALUES(#{id}, #{creatTime}, #{content}, #{creatPeople})")
    int insert(LoveTime loveTime);

    /**
     * 获取所有恋爱时光记录
     */
    @Select("SELECT * FROM love_time ORDER BY creat_time DESC")
    List<LoveTime> selectAll();

    /**
     * 根据发起人获取记录
     */
    @Select("SELECT * FROM love_time WHERE creat_people = #{creatPeople} ORDER BY creat_time DESC")
    List<LoveTime> selectByCreatPeople(String creatPeople);

    /**
     * 根据ID删除记录
     */
    @Delete("DELETE FROM love_time WHERE id = #{id}")
    int deleteById(String id);
}
