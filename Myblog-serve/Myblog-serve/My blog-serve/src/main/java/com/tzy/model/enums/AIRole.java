package com.tzy.model.enums;

import lombok.Getter;

/**
 * AI 助手角色枚举
 * 定义了不同类型的 AI 助手及其对应的系统提示语
 * @author tzy
 * @since 2024.2.8
 */
@Getter
public enum AIRole {
    /** 通用 AI 助手，适合一般性对话 */
    GENERAL("general", "通用助手", "你是一个友善的AI助手，可以帮助用户解答各种问题。请用简洁明了的方式回答。"),
    
    /** 程序员助手，专注于编程相关问题 */
    PROGRAMMER("programmer", "程序员", "你是一个专业的程序员助手，精通各种编程语言和技术。请用专业且易懂的方式回答技术问题。"),
    
    /** 写作助手，帮助改进文章 */
    WRITER("writer", "写作助手", "你是一个专业的写作助手，可以帮助用户改进文章结构和用词。请用优美的语言帮助用户。"),
    
    /** 翻译助手，专注于语言翻译 */
    TRANSLATOR("translator", "翻译助手", "你是一个专业的翻译助手，精通中英文互译。请准确传达原文含义，同时保持译文的流畅自然。"),
    
    /** 教师助手，擅长解释概念 */
    TEACHER("teacher", "教师助手", "你是一个耐心的教师助手，擅长将复杂的概念简化解释。请用循序渐进的方式回答问题。");

    /** 角色代码，用于标识不同的角色 */
    private final String code;
    
    /** 角色名称，用于显示 */
    private final String name;
    
    /** 角色的系统提示语，用于指导 AI 的回答方式 */
    private final String prompt;

    AIRole(String code, String name, String prompt) {
        this.code = code;
        this.name = name;
        this.prompt = prompt;
    }

    /**
     * 根据角色代码获取对应的角色枚举
     * @param code 角色代码
     * @return 对应的角色枚举，如果未找到则返回通用助手
     */
    public static AIRole getByCode(String code) {
        for (AIRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return GENERAL;
    }
} 