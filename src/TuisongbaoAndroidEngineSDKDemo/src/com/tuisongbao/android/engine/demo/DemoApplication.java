package com.tuisongbao.android.engine.demo;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Application;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.TSBEngineOptions;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // remote service 启动时会有第二次 onCreate 的调用
        // 为了解决这个问题，可以根据 process name 来防止SDK被初始化2次
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null
                || !processName.equalsIgnoreCase("com.tuisongbao.android.engine.demo")) {
            return;
        }

        // 初始化与 server 的连接
        // appId是在推送宝官网注册应用时分配的ID
        TSBEngineOptions options = new TSBEngineOptions("ab3d5241778158b2864c0852" , "http://staging.tuisongbao.com/api/engineDemo/authUser");
        TSBEngine.init(this, options);
        // or, TSBEngine.init(this, appId, authUrl); authUrl用于鉴权, 推荐用https, 参见
        // 登陆 一节
    }

    private String getProcessName(int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am
                .getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
                    .next());
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
            }
        }
        return processName;
    }
}
