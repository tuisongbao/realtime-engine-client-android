package com.tuisongbao.engine.chat.message;

/**
 * <STRONG>语音消息内容</STRONG>
 */
public class ChatMessageVoiceContent extends ChatMessageMediaContent {
    public ChatMessageVoiceContent() {
        setType(ChatMessage.TYPE.VOICE);
    }

    /**
     * 获取语音时长
     *
     * @return 语音时长，单位是 “秒”
     */
    public double getDuration() {
        return getFile().getDuration();
    }
}
