package com.tuisongbao.engine.chat.user;

import com.tuisongbao.engine.utils.StrUtils;

/**
 * 聊天类型，单聊 或 群聊
 */
public enum ChatType {
    GroupChat("groupChat", 1), SingleChat("singleChat", 2);

    private String name;
    private int index;

    ChatType(String name, int index) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static ChatType getType(String name) {
        if (!StrUtils.isEmpty(name)) {
            ChatType[] types = values();
            for (ChatType type : types) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
        }
        return null;
    }
}
