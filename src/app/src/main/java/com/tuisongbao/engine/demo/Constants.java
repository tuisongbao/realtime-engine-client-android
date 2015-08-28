package com.tuisongbao.engine.demo;

/**
 * Created by user on 15-8-14.
 */
public interface Constants {

    /** SharedPreferences 文件名 **/
    String SHARED_PREFERENCE_NAME = "client_preferences";

    /********************************** 用户登陆管理 ***************************************************************************************************/

    /** 用户最后登陆时间 **/
    String LAST_TIME = "LAST_TIME";

    /** 一个月内自动登陆 : 60 * 60 * 24 * 30 = 2592000 **/
    long AUTO_LOGIN = 2592000;

    /** 记录上次退出时页面 **/
    String PAGENUMBER = "PAGENUMBER";

    String APPID = "ab3d5241778158b2864c0852";

    String APIURL = "http://www.tuisongbao.com/api/engineDemo";

    String AUTHUSERURL = "http://www.tuisongbao.com/api/engineDemo/authUser";

    String USERAVATARURL = "http://www.tuisongbao.com/engine/demo/chatUserAvatar?id=";

    String AUTOLOGINUSERNAME = "AUTOLOGINUSERNAME";
}
