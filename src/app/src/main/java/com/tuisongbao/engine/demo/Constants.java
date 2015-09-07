package com.tuisongbao.engine.demo;

/**
 * Created by user on 15-8-31.
 */
public interface Constants {
    // 推送
    String APPID = "ab3d5241778158b2864c0852";
    String BASEURL = "192.168.222.20";
    String AUTHUSERURL = "http://" + BASEURL + "/api/engineDemo/authUser";

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
    String NET_ERROR = "网络错误，请稍后再试！";


    String USERAVATARURL = "http://" + BASEURL + "/engine/demo/chatUserAvatar?id=";
    String APIURL = "http://" + BASEURL + "/api/engineDemo";
    String CREATEGROUPAPI = "http://" + BASEURL + "/api/engineDemo/createChatGroup";
    String uploadChatUserAvatar = "http://" + BASEURL + "/api/engineDemo/uploadChatUserAvatar";
    String changPasswordUrl =  "http://" + BASEURL + "/api/engineDemo/changePassword";
}
