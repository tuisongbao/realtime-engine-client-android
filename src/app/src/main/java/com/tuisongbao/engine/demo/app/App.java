package com.tuisongbao.engine.demo.app;


import android.app.ActivityManager;
import android.app.Application;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.EngineOptions;
import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatUser;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.utils.LogUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 15-8-18.
 */
public class App extends Application {
    private static final String LOGTAG = LogUtil.makeLogTag(App.class);
    private static App instance;
    private Engine engine;
    private List<ChatGroup> chatGroups;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

//        // 异常处理，不需要处理时注释掉这两句即可！
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        // 注册crashHandler
//        crashHandler.init(getApplicationContext());

        // remote service 启动时会有第二次 onCreate 的调用
        // 为了解决这个问题，可以根据 process name 来防止SDK被初始化2次
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null
                || !processName.equalsIgnoreCase("com.tuisongbao.engine.demo")) {
            return;
        }

        initImageLodaer();

        // 初始化 EngineOptions
        // appId 是在推送宝官网注册应用时分配的 ID；authUrl 用于鉴权, 推荐用 https, 参见 登录 一节
        EngineOptions options = new EngineOptions(Constants.APPID , Constants.AUTHUSERURL
        );

        engine = new Engine(this, options);

        ChatManager chatManager = engine.getChatManager();
        chatManager.enableCache();
    }

    private void initImageLodaer(){

        DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.demo_logo) // resource or drawable
            .showImageForEmptyUri(R.drawable.demo_logo) // resource or drawable
            .showImageOnFail(R.drawable.demo_logo) // resource or drawable
            .delayBeforeLoading(1000)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
            .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
            .threadPoolSize(3)//线程池内加载的数量
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .denyCacheImageMultipleSizesInMemory()
            .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
            .memoryCacheSize(2 * 1024 * 1024)
            .tasksProcessingOrder(QueueProcessingType.LIFO)
            .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
            .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
            .writeDebugLogs() // Remove for release app
            .defaultDisplayImageOptions(options)
            .build();//开始构建

        ImageLoader.getInstance().init(config);
    }

    /**
     * Global context
     *
     * @return
     */
    public static App getContext() {
        return instance;
    }

    public Engine getEngine() {
        return engine;
    }

    public ChatUser getUser() {
        return getChatManager().getChatUser();
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

    public ChannelManager getChannelManager() {
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

    public List<ChatGroup> getChatGroups() {
        return chatGroups;
    }

    public void setChatGroups(List<ChatGroup> chatGroups) {
        this.chatGroups = chatGroups;
    }
}
