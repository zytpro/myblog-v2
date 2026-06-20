package com.tzy.mapper;

import com.tzy.pojo.FreeInterface;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApiMapper {
    //获取接口列表
    @Select("SELECT * FROM freeinterface")
    List<FreeInterface> getApiList();

    //添加接口和链接
    @Insert("INSERT INTO freeinterface(name, api) VALUES(#{name}, #{link})")
    Boolean addLink(@Param("name") String name, @Param("link") String link);

    //删除接口和链接
    @Delete("DELETE FROM freeinterface WHERE id = #{id}")
    Boolean deleteLink(String id);
}
