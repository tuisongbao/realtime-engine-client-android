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
    public static List<DemoGroup> ListGroupInfos = new ArrayList<DemoGroup>();// 群聊信息
    public static Map<String, DemoGroup> GroupInfos = new HashMap<String, DemoGroup>();
}
