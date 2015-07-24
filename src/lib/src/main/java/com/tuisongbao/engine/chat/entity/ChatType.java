package com.tuisongbao.engine.chat.entity;

import com.tuisongbao.engine.util.StrUtil;

public enum ChatType {
    GroupChat("groupChat", 1), SingleChat("singleChat", 2);

    private String name;
    private int index;

    private ChatType(String name, int index) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static ChatType getType(String name) {
        if (!StrUtil.isEmpty(name)) {
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
