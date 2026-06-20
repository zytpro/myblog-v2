package com.tzy.mapper;

import com.tzy.pojo.LoveRoom;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoveRoomMapper {

    @Select("SELECT * FROM love_room LIMIT 1")
    LoveRoom getLoveRoomInfo();

    @Insert("INSERT INTO love_room(boy_name, girl_name, boy_avatar, girl_avatar, time) " +
            "VALUES(#{boyName}, #{girlName}, #{boyAvatar}, #{girlAvatar}, #{time})")
    int insertLoveRoom(LoveRoom loveRoom);

    @Update("UPDATE love_room SET boy_name=#{boyName}, girl_name=#{girlName}, " +
            "boy_avatar=#{boyAvatar}, girl_avatar=#{girlAvatar}, time=#{time}")
    int updateLoveRoom(LoveRoom loveRoom);

    @Select("SELECT COUNT(*) FROM love_room")
    int count();
}
