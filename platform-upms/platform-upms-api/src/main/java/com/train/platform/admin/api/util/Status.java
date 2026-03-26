package com.train.platform.admin.api.util;

public enum Status {
    TRAINING("训练中"), PAUSED("暂停中"), COMPLETED("已完成");

    private final String description;

    // 私有构造函数，用于在枚举常量中设置描述
    private Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
