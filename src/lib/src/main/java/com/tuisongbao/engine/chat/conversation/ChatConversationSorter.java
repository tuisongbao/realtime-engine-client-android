package com.tuisongbao.engine.chat.conversation;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.Comparator;
import java.util.Date;

/**
 * 会话排序器，根据 {@link ChatConversation#lastActiveAt} 逆序排列
 */
class ChatConversationSorter implements Comparator<ChatConversation> {
    @Override
    public int compare(ChatConversation lhs, ChatConversation rhs) {
        Date lLastActiveDate = StrUtils.getDateFromTimeStringIOS8061(lhs.getLastActiveAt());
        Date rLastActiveDate = StrUtils.getDateFromTimeStringIOS8061(rhs.getLastActiveAt());
        if (lLastActiveDate.after(rLastActiveDate)) {
            return -1;
        } else {
            return 1;
        }
    }
}
