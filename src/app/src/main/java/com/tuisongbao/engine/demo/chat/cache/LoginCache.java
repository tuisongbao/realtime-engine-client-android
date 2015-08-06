package com.tuisongbao.engine.demo.chat.cache;

import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

public class LoginCache {

    private static String userId;
    private static List<ChatUser> mAddList = new ArrayList<ChatUser>();

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        LoginCache.userId = userId;
    }

    public static List<ChatUser> getAddedUserList() {
        return mAddList;
    }

    public static void addUser(ChatUser user) {
        mAddList.add(user);
    }

    public static boolean isLogin() {
        return !StrUtils.isEmpty(userId);
    }

    public static void clear() {
        userId = "";
        mAddList.clear();
    }
}
