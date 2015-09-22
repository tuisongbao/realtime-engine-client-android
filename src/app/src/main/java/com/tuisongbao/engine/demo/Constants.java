package com.tuisongbao.engine.demo;

/**
 * Created by user on 15-8-31.
 */
public interface Constants {
    // 推送宝AppId
    String APPID = "ab3d5241778158b2864c0852";

    // 聊天
    String LOGINSTATE = "LOGINSTATE";
    String USERINFO = "USERINFO";
    String ACCESSTOKEN = "ACCESSTOKEN";
    String PWD = "PWD";
    String USERNAME = "USERNAME";

    // Demo Base Url
    String BASEURL = "http://www.tuisongbao.com";

    // 头像
    String USERAVATARURL = BASEURL + "/engine/demo/chatUserAvatar?id=";

    // Demo Api
    String NETERROR = "网络错误，请稍后再试！";
    String APIURL = BASEURL + "/api/engineDemo";
    String AUTHUSERURL = APIURL + "/authUser";
    String CREATEGROUPURL = APIURL + "/createChatGroup";
    String UPLOADCHATUSERAVATARURL = APIURL + "/uploadChatUserAvatar";
    String CHANGPASSWORDURL =  APIURL + "/changePassword";
    String REGISTURL =  APIURL + "/registerChatUser";
    String LOGINURL = APIURL + "/validateChatUser";
    String DEMOGROUPURL = APIURL + "/getChatGroups";
    String DEMOUSERURL = APIURL + "/searchChatUsers";
}
