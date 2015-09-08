package com.tuisongbao.engine.demo;

/**
 * Created by user on 15-8-31.
 */
public interface Constants {
    // 推送
    String APPID = "ab3d5241778158b2864c0852";

    // 聊天
    String LoginState = "LoginState";
    String UserInfo = "UserInfo";
    String AccessToken = "AccessToken";
    String PWD = "PWD";
    String User_ID = "User_ID";

    // JSON status
    String Info = "info";
    String Value = "data";
    String Result = "status";

    // Demo Base Url
    String BaseUrl = "http://192.168.222.20";

    // 头像
    String USERAVATARURL = BaseUrl + "/engine/demo/chatUserAvatar?id=";

    // Demo Api
    String NET_ERROR = "网络错误，请稍后再试！";
    String APIURL = BaseUrl + "/api/engineDemo";
    String AUTHUSERURL = APIURL + "/authUser";
    String CREATEGROUPAPI = APIURL + "/createChatGroup";
    String uploadChatUserAvatar = APIURL + "/uploadChatUserAvatar";
    String changPasswordUrl =  APIURL + "/changePassword";
    String RegistURL =  APIURL + "/registerChatUser";
    String Login_URL = APIURL + "/validateChatUser";
}
