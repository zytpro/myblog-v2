package com.tzy.service.impl;

import com.tzy.mapper.MessageMapper;
import com.tzy.pojo.Message;
import com.tzy.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> getAllMessages() {
        return messageMapper.getAllMessages();

        }

    @Override
    public Boolean addMessage(String message) {
        Message message1 = new Message();
        message1.setMessage(message);
        String time = LocalDateTime.now().toString();
        message1.setCreateTime(time);
        return messageMapper.addMessage(message1);
    }
}
