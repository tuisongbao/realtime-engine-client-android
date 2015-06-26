package com.tuisongbao.android.engine.chat.entity;

import com.tuisongbao.android.engine.util.StrUtil;

public enum TSBChatEvent {
    FriendAdded("friend:added", 1),

    GroupJoined("group:joined", 2),
    GroupRemoved("group:removed", 3),
    GroupDismissed("group:dismissed", 4);

    private String name;
    private int index;

    TSBChatEvent(String name, int index) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static TSBChatEvent getType(String name) {
        if (!StrUtil.isEmpty(name)) {
            TSBChatEvent[] types = values();
            for (TSBChatEvent type : types) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
        }
        return null;
    }
}
