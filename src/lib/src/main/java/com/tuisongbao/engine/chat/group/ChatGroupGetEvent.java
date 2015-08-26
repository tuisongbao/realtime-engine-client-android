package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatGroupGetEvent extends BaseEvent<ChatGroupEventData> {
    private static final String TAG = ChatGroupGetEvent.class.getSimpleName();

    public ChatGroupGetEvent() {
        super("engine_chat:group:get");

        serializeFields.add("groupId");
        serializeFields.add("lastActiveAt");
    }
}
