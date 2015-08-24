package com.tuisongbao.engine.demo.utils;

import com.tuisongbao.engine.demo.entity.DemoGroup;
import com.tuisongbao.engine.demo.service.rest.UserService;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 15-8-24.
 */
@EBean(scope = EBean.Scope.Singleton)
public class DemoGroupUtil {
    private static final String TAG = LogUtil.makeLogTag(DemoGroupUtil.class);

    @RestService
    UserService userService;

    private Map<String, DemoGroup> groups = new HashMap<>();

    public Map<String, DemoGroup> getDemoGroups() {
        return groups;
    }

    public DemoGroup getDemoGroup(String groupId) {
        DemoGroup demoGroup = null;

        if (groups.containsKey(groupId)) {
            demoGroup = groups.get(groupId);
        } else {
            List<String> ids = new ArrayList<>();
            ids.add(groupId);
            List<DemoGroup> groupList = getDemoGroups(ids);
            if (groupList != null && !groupList.isEmpty()) {
                demoGroup = groupList.get(0);
            }
        }
        return demoGroup;
    }

    public List<DemoGroup> getDemoGroups(List<String> ids) {
        if (ids == null) {
            return null;
        }
        List<DemoGroup> groupList = null;
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.put("groupIds[]", ids);

        try {
            groupList = userService.getGroupDemoInfo(map);
        } catch (Exception e) {
            L.i(TAG, "Exception ----------------------" + e);
        }

        if (groupList != null) {
            for (DemoGroup group : groupList) {
                groups.put(group.getGroupId(), group);
            }
        }

        return groupList;
    }
}
