package com.tzy.service;

import com.tzy.pojo.Message;
import com.tzy.pojo.Result;

import java.util.List;


public interface MessageService  {

    List<Message> getAllMessages();

    Boolean addMessage(String message);
}
