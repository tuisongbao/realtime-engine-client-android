package com.tuisongbao.engine.demo.common.utils;

import android.content.Context;

import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.demo.R;

public class ChatMessageUtils {
    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(ChatMessage message, Context context) {
        String digest = "";
        switch (message.getContent().getType()) {
            case LOCATION: // 位置消息
                digest = getString(context, R.string.location_message);
                break;
            case IMAGE: // 图片消息
                digest = getString(context, R.string.picture);
                break;
            case VOICE:// 语音消息
                digest = getString(context, R.string.voice_msg);
                break;
            case VIDEO: // 视频消息
                digest = getString(context, R.string.video);
                break;
            case EVENT: // 事件消息
                digest = getString(context, R.string.event);
                break;
            case TEXT: // 文本消息
                digest = message.getContent().getText();
                break;
            default:
                System.err.println("error, unknow type");
                return "";
        }
        return digest;
    }

    static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }
}
