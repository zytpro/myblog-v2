package com.tzy.mapper;

import com.tzy.pojo.Navbar;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NavbarMapper {
    
    @Select("SELECT * FROM navbar")
    List<Navbar> findAll();
    
    @Select("SELECT * FROM navbar WHERE name = #{name}")
    Navbar findByName(String name);
    
    @Insert("INSERT INTO navbar(name, state) VALUES(#{name}, #{state})")
    int insert(Navbar navbar);
    
    @Update("UPDATE navbar SET state = #{state} WHERE name = #{name}")
    int update(Navbar navbar);
    
    @Delete("DELETE FROM navbar WHERE name = #{name}")
    int delete(String name);
} 