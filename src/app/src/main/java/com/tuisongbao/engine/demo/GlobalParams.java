package com.tuisongbao.engine.demo;

import com.tuisongbao.engine.demo.group.entity.DemoGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 15-8-31.
 */
public class GlobalParams {
    public static List<DemoGroup> ListGroupInfo = new ArrayList<DemoGroup>();// 群聊信息
    public static Map<String, DemoGroup> GroupInfo = new HashMap<String, DemoGroup>();
}
