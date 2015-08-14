package com.tuisongbao.engine.chat.user;

import com.tuisongbao.engine.utils.StrUtils;

/**
 * 聊天类型，单聊 或 群聊
 */
public enum ChatType {
    GroupChat("groupChat"),
    SingleChat("singleChat"),
    Unknown("Unknown");

    private String name;

    ChatType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ChatType getType(String name) {
        if (!StrUtils.isEmpty(name)) {
            for (ChatType type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
        }
        return Unknown;
    }
}
