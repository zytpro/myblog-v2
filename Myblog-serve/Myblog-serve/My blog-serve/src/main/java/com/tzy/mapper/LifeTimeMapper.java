package com.tzy.mapper;

import com.tzy.pojo.LifeTime;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 生活时光数据访问接口
 *
 * @author tzy
 * @date 2024/04/05
 */
@Mapper
public interface LifeTimeMapper {

    /**
     * 添加一条生活时光记录
     */
    @Insert("INSERT INTO life_time(content, url, create_time) VALUES(#{content}, #{url}, NOW())")
    int insert(LifeTime lifeTime);

    /**
     * 获取所有生活时光记录
     */
    @Select("SELECT * FROM life_time")
    List<LifeTime> selectAll();

    /**
     * 根据照片链接删除记录
     */
    @Delete("DELETE FROM life_time WHERE url = #{url}")
    int deleteByUrl(String url);

    /**
     * 更新生活时光记录
     */
    @Update("UPDATE life_time SET content = #{content} WHERE url = #{url}")
    int updateByUrl(LifeTime lifeTime);
}
