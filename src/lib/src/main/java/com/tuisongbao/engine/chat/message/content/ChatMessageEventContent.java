package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.utils.StrUtils;

public class ChatMessageEventContent extends ChatMessageContent {
    public ChatMessageEventContent() {
        setType(ChatMessage.TYPE.EVENT);
    }

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

        @Override
        public String toString() {
            return name;
        }
    }

    public TYPE getEventType() {
        return getEvent().getType();
    }

    public String getTarget() {
        return getEvent().getTarget();
    }
}
