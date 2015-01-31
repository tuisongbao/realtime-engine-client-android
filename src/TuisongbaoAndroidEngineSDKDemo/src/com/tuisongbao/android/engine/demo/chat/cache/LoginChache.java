package com.tuisongbao.android.engine.demo.chat.cache;

import java.util.ArrayList;
import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;

public class LoginChache {

    private static String userId;
    private static List<TSBChatGroupUser> mAddList = new ArrayList<TSBChatGroupUser>();

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        LoginChache.userId = userId;
    }

    public static List<TSBChatGroupUser> getAddedUserList() {
        return mAddList;
    }
    
    public static void addUser(TSBChatGroupUser user) {
        mAddList.add(user);
    }
}
