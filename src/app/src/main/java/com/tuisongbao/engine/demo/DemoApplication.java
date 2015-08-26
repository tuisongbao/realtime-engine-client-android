package com.tuisongbao.engine.demo;

import android.app.ActivityManager;
import android.app.Application;

import com.tuisongbao.android.PushManager;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.EngineOptions;
import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroupManager;

import java.util.Iterator;
import java.util.List;

public class DemoApplication extends Application {
    public static Engine engine;
    private static final String TAG = DemoApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        PushManager.init(this);

        // 启用 Push 时会有第二次 onCreate 的调用，可以根据 process name 来防止 Engine SDK被初始化2次
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null
                || !processName.equalsIgnoreCase("com.tuisongbao.engine.demo")) {
            return;
        }
        // 初始化 EngineOptions
        // appId 是在推送宝官网注册应用时分配的 ID；authUrl 用于鉴权, 推荐用 https, 参见 登录 一节
        EngineOptions options = new EngineOptions("ab3d5241778158b2864c0852" , "http://192.168.225.102/api/engineDemo/authUser"
        );
        engine = new Engine(this, options);

        ChatManager chatManager = engine.getChatManager();
        chatManager.enableCache();
    }

    public static ChatGroupManager getGroupManager() {
        return engine.getChatManager().getGroupManager();
    }

    public static ChatConversationManager getConversationManager() {
        return engine.getChatManager().getConversationManager();
    }

    public static ChannelManager getChannelManager() {
        return engine.getChannelManager();
    }

    private String getProcessName(int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (i.next());
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
