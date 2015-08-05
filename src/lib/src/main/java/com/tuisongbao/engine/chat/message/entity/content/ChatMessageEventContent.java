package com.tuisongbao.engine.chat.message.entity.content;

public class ChatMessageEventContent {
    public enum TYPE {
        FriendAdded("friend:added"),

        GroupJoined("group:joined"),
        GroupRemoved("group:removed"),
        GroupDismissed("group:dismissed");

        private String name;

        TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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

    public TYPE getType(String name) {
        return TYPE.valueOf(name);
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
}
