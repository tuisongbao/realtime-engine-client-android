package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.chat.ChatUserPresence;
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

    /**
     * 获取用户唯一标识
     *
     * @return  用户唯一标识
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取用户在线状态
     *
     * @return  用户在线状态
     */
    public ChatUserPresence.Presence getPresence() {
        return presence;
    }
}
