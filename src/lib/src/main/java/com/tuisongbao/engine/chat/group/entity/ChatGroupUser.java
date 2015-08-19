package com.tuisongbao.engine.chat.group.entity;

import com.tuisongbao.engine.chat.user.entity.ChatUserPresence;
import com.tuisongbao.engine.common.callback.EngineCallback;

/**
 * <STRONG> {@link ChatGroup} 成员实体类 </STRONG>
 *
 * <P>
 *     获取群组成员列表的返回参数为该类的集合
 * </P>
 *
 * @see ChatGroup#getUsers(EngineCallback)
 */
public class ChatGroupUser {
    private String userId;
    private ChatUserPresence.Presence presence;

    public String getUserId() {
        return userId;
    }

    public ChatUserPresence.Presence getPresence() {
        return presence;
    }
}
