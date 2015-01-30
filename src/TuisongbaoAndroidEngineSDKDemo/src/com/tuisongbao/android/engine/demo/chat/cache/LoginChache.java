package com.tuisongbao.android.engine.demo.chat.cache;

public class LoginChache {

    private static String userId;

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        LoginChache.userId = userId;
    }
}
