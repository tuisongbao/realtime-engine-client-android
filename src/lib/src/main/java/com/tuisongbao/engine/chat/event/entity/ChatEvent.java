package com.tuisongbao.engine.chat.event.entity;

import com.tuisongbao.engine.util.StrUtil;

public enum ChatEvent {
    FriendAdded("friend:added", 1),

    GroupJoined("group:joined", 2),
    GroupRemoved("group:removed", 3),
    GroupDismissed("group:dismissed", 4);

    private String name;
    private int index;

    ChatEvent(String name, int index) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static ChatEvent getType(String name) {
        if (!StrUtil.isEmpty(name)) {
            ChatEvent[] types = values();
            for (ChatEvent type : types) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
        }
        return null;
    }
}
