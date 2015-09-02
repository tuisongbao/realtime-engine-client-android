package com.tuisongbao.engine.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.EngineOptions;
import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatUser;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroupManager;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by user on 15-8-31.
 */
public class App extends Application{
    private static Context _context;

    private Engine engine;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        _context = getApplicationContext();
        // CrashHandler crashHandler = CrashHandler.getInstance();// 全局异常捕捉
        // crashHandler.init(_context);
        initEngine();
    }


    public Engine getEngine() {
        if(engine == null){
            initEngine();
        }
        return engine;
    }

    public ChatManager getChatManager() {
        return engine.getChatManager();
    }

    public ChatGroupManager getGroupManager() {
        return engine.getChatManager().getGroupManager();
    }

    public ChatConversationManager getConversationManager() {
        return engine.getChatManager().getConversationManager();
    }

    public ChatUser getChatUser(){
        return getChatManager().getChatUser();
    }

    public ChannelManager getChannelManager() {
        return engine.getChannelManager();
    }

    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
                    .next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm
                            .getApplicationInfo(info.processName,
                                    PackageManager.GET_META_DATA));
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
            }
        }
        return processName;
    }

    private void initEngine(){
        // 初始化 EngineOptions
        // appId 是在推送宝官网注册应用时分配的 ID；authUrl 用于鉴权, 推荐用 https, 参见 登录 一节
        EngineOptions options = new EngineOptions(Constants.APPID , Constants.AUTHUSERURL
        );
        engine = new Engine(this, options);
        engine.getChatManager().enableCache();
    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            deleteCacheDirFile(getDemoCacheDir(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.gc();
    }

    public static Context getInstance() {
        return _context;
    }

    // 运用list来保存们每一个activity是关键
    private List<Activity> mList = new LinkedList<Activity>();
    private static App instance;

    // 构造方法
    // 实例化一次
    public synchronized static App getInstance2() {
        if (null == instance) {
            instance = new App();
        }
        return instance;
    }

    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    // 关闭每一个list内的activity
    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static String getDemoCacheDir() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
            return Environment.getExternalStorageDirectory().toString()
                    + "/Health/Cache";
        else
            return "/System/com.tuisongbao.engine/Walk/Cache";
    }

    public static String getDemoDownLoadDir() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
            return Environment.getExternalStorageDirectory().toString()
                    + "/Walk/Download";
        else {
            return "/System/com.tuisongbao.engine/Walk/Download";
        }
    }

    public static void deleteCacheDirFile(String filePath,
                                          boolean deleteThisPath) throws IOException {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.isDirectory()) {// 处理目录
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteCacheDirFile(files[i].getAbsolutePath(), true);
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory()) {// 如果是文件，删除
                    file.delete();
                } else {// 目录
                    if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                        file.delete();
                    }
                }
            }
        }
    }
}
