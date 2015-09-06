package com.tuisongbao.engine.demo;

/**
 * Created by user on 15-8-31.
 */
public interface Constants {
    // 推送包
    String APPID = "ab3d5241778158b2864c0852";
    String BASEURL = "192.168.222.20";
    String AUTHUSERURL = "http://" + BASEURL + "/api/engineDemo/authUser";
    // 聊天
    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    String isFriend = "isFriend";
    String LoginState = "LoginState";
    String UserInfo = "UserInfo";
    String URL = "URL";
    String Title = "Title";
    String ID = "id";
    String NAME = "NAME";
    String AccessToken = "AccessToken";
    String PWD = "PWD";
    String ContactMsg = "ContactMsg";
    String VersionInfo = "VersionInfo";
    String SeeMe = "SeeMe";
    String LokeMe = "LokeMe";
    String IsMsg = "IsMsg";
    String IsVideo = "IsVideo";
    String IsZhen = "IsZhen";
    String User_ID = "User_ID";
    String GROUP_ID = "GROUP_ID";
    String TYPE = "TYPE";
    // JSON status
    String Info = "info";
    String Value = "data";
    String Result = "status";
    String DB_NAME = "WeChat.db";
    String NET_ERROR = "网络错误，请稍后再试！";
    String BaiduPullKey = "Uvw5AMP15i9v1cUoS5aY7GR1";
    // 主机地址
    // public static String IP = "http://wechatjuns.sinaapp.com/";
    // String MAIN_ENGINE = "http://10.16.16.79/wechat/index.php/mobile/";
    String MAIN_ENGINE = "http://" + BASEURL + "/api/engineDemo/";

    // 发送验证码 codeType 1注册 2修改密码
    String SendCodeURL = "";
    // 用户注册
    String RegistURL = MAIN_ENGINE + "registerChatUser";
    // 用户登录
    String Login_URL = MAIN_ENGINE + "validateChatUser";
    // 更新用户信息
    String UpdateInfoURL = MAIN_ENGINE + "user/update_userinfo";
    // 获取用户信息
    String getUserInfoURL = MAIN_ENGINE + "user/get_user_list";
    // 检查版本
    String getVersionURL = MAIN_ENGINE + "020";
    // 更新用户信息
    String updateUserInfoURL = MAIN_ENGINE + "004";
    // 我的群组
    String getGroupListURL = MAIN_ENGINE + "group/get_group_list";
    // 反馈
    String FeedbackURL = MAIN_ENGINE + "019";
    // 获取群组信息
    String getGroupInfoURL = MAIN_ENGINE + "027";

    // 查询所有好友033
    String getContactFriendURL = MAIN_ENGINE + "user/get_contact_list";
    // 举报018
    String JuBaoURL = MAIN_ENGINE + "018";
    // 加入群组
    String addGroupURL = MAIN_ENGINE + "031";
    // 退出群组
    String exitGroupURL = MAIN_ENGINE + "029";
    // 新建群组
    String newGroupURL = MAIN_ENGINE + "group/add_group";

    String USERAVATARURL = "http://" + BASEURL + "/engine/demo/chatUserAvatar?id=";
    String DEMOGROUPINFOURL = "http://" + BASEURL + "/api/engineDemo/getChatGroups";
    String APIURL = "http://" + BASEURL + "/api/engineDemo";
    String CREATEGROUPAPI = "http://" + BASEURL + "/api/engineDemo/createChatGroup";
}
