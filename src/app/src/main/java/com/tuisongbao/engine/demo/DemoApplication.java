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
import com.tuisongbao.engine.demo.chat.service.ChatMessageRevieveService;

import java.util.Iterator;
import java.util.List;

public class DemoApplication extends Application {
    public static Engine engine;

    @Override
    public void onCreate() {
        super.onCreate();

        PushManager.init(getApplicationContext());

        // remote service 启动时会有第二次 onCreate 的调用
        // 为了解决这个问题，可以根据 process name 来防止SDK被初始化2次
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null
                || !processName.equalsIgnoreCase("com.tuisongbao.engine.demo")) {
            return;
        }

        // appId是在推送宝官网注册应用时分配的ID
        EngineOptions options = new EngineOptions("ab3d5241778158b2864c0852" , "http://192.168.225.102/api/engineDemo/authUser");
        options.setChatIntentService(ChatMessageRevieveService.class);
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
