package com.tuisongbao.android.engine.chat.entity;


public class TSBChatConversationData {
    /**
     * 可选， Conversation 类型， singleChat（单聊） 或 groupChat （群聊）
     */
    private ChatType type;
    /**
     * 可选，跟谁， userId 或 groupId
     */
    private String target;
    private String lastActiveAt;

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setLastActiveAt(String lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }
}
