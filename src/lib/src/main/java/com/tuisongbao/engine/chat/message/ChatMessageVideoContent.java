package com.tuisongbao.engine.chat.message;

/**
 * <STRONG>视频消息内容</STRONG>
 */
public class ChatMessageVideoContent extends ChatMessageMediaContent {
    public ChatMessageVideoContent() {
        setType(ChatMessage.TYPE.VIDEO);
    }

    /**
     * 获取视频时长
     *
     * @return 语音时长，单位是 “秒”
     */
    public double getDuration() {
        return getFile().getDuration();
    }
}
