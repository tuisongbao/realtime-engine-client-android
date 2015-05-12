package com.tuisongbao.android.engine.demo.chat.cache;

import java.util.ArrayList;
import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBContactsUser;
import com.tuisongbao.android.engine.util.StrUtil;

public class LoginChache {

    private static String userId;
    private static List<TSBContactsUser> mAddList = new ArrayList<TSBContactsUser>();

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        LoginChache.userId = userId;
    }

    public static List<TSBContactsUser> getAddedUserList() {
        return mAddList;
    }

    public static void addUser(TSBContactsUser user) {
        mAddList.add(user);
    }

    public static boolean isLogin() {
        return !StrUtil.isEmpty(userId);
    }

    public static void clear() {
        userId = "";
        mAddList.clear();
    }
}
