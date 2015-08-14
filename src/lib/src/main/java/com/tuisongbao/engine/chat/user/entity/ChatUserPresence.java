package com.tuisongbao.engine.chat.user.entity;

/**
 * <STRONG>Chat 用户上下线通知实体类</STRONG>
 *
 * <P>
 *     在 {@link com.tuisongbao.engine.chat.ChatManager#EVENT_PRESENCE_CHANGED} 事件处理方法中会使用。
 */
public class ChatUserPresence {
    public enum Presence {
        Offline("offline"),
        Online("online"),
        Unknown("unknown");

        private final String name;

        Presence(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Presence getPresence(String name) {
            for (Presence presence : values()) {
                if (presence.name.equals(name)) {
                    return presence;
                }
            }
            return Unknown;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private String userId;
    private Presence changedTo;

    public String getUserId() {
        return userId;
    }

    public Presence getChangedTo() {
        return changedTo;
    }
}
