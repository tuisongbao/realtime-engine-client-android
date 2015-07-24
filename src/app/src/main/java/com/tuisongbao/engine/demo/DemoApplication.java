package com.tuisongbao.engine.demo;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Application;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.TSBEngineOptions;
import com.tuisongbao.engine.demo.chat.service.TSBMessageRevieveService;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // remote service 启动时会有第二次 onCreate 的调用
        // 为了解决这个问题，可以根据 process name 来防止SDK被初始化2次
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null
                || !processName.equalsIgnoreCase("com.tuisongbao.engine.demo")) {
            return;
        }

        // appId是在推送宝官网注册应用时分配的ID
        TSBEngineOptions options = new TSBEngineOptions("ab3d5241778158b2864c0852" , "http://192.168.225.102/api/engineDemo/authUser");
        options.setChatIntentService(TSBMessageRevieveService.class);
        TSBEngine.init(this, options);

    }

    private String getProcessName(int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am
                .getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (i
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
