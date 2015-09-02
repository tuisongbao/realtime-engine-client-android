package com.tuisongbao.engine.demo;

import com.tuisongbao.engine.demo.bean.DemoGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 15-8-31.
 */
public class GloableParams {
    // 屏幕高度 宽度
    public static int WIN_WIDTH;
    public static int WIN_HEIGHT;
//    public static Map<String, User> Users = new HashMap<String, User>();
//    public static List<User> UserInfos = new ArrayList<User>();// 好友信息
    public static List<DemoGroup> ListGroupInfos = new ArrayList<DemoGroup>();// 群聊信息
    public static Map<String, DemoGroup> GroupInfos = new HashMap<String, DemoGroup>();
    public static Boolean isHasPulicMsg = false;
}
