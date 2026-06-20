package com.tzy.service;

import com.tzy.enums.AIType;
import java.util.List;
import java.util.Map;

public interface ChatService {
    String chat(String message, Integer type);
    List<Map<String, Object>> getAITypes();
} 