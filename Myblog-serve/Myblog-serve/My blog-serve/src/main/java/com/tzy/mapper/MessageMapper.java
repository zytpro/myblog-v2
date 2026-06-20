package com.tzy.mapper;

import com.tzy.pojo.Message;
import com.tzy.pojo.Result;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询所有的 message 表数据,较早的数据排在前
    @Select("SELECT * FROM message ORDER BY create_time ASC")
    List<Message> getAllMessages();

    @Insert("INSERT INTO message(message, create_time) VALUES(#{message}, #{createTime})")
    Boolean addMessage(Message message);
}
