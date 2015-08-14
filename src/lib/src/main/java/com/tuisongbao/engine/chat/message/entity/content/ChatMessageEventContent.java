package com.tuisongbao.engine.chat.message.entity.content;

import com.tuisongbao.engine.utils.StrUtils;

public class ChatMessageEventContent {
    public enum TYPE {
        FriendAdded("friend:added"),

        GroupJoined("group:joined"),
        GroupRemoved("group:removed"),
        GroupDismissed("group:dismissed"),
        Unknown("unknown");

        private String name;

        TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static TYPE getType(String name) {
            if (!StrUtils.isEmpty(name)) {
                TYPE[] types = values();
                for (TYPE type : types) {
                    if (type.getName().equals(name)) {
                        return type;
                    }
                }
            }
            return Unknown;
        }
    }

    private TYPE type;
    private String target;

    public String getTarget() {
        return target;
    }

    public TYPE getType() {
        return type;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
}
