package com.tzy.enums;

public enum AIType {
    GENERAL(1, "通用AI", "deepseek/deepseek-chat:free"),
    GIRLFRIEND(2, "暖心女友", "deepseek/deepseek-chat:free"),
    FIREFLY(3, "流萤", "deepseek/deepseek-chat:free");

    private final int code;
    private final String name;
    private final String modelId;

    AIType(int code, String name, String modelId) {
        this.code = code;
        this.name = name;
        this.modelId = modelId;
    }

    public static AIType getByCode(int code) {
        for (AIType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid AI type code: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getModelId() {
        return modelId;
    }
} 