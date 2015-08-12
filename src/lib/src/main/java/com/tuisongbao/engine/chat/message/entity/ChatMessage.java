package com.tuisongbao.engine.chat.message.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileContent;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageContentSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageEventTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.DownloadUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * <STRONG>Chat 消息</STRONG>
 *
 * <UL>
 *     <LI>提供了多媒体消息的资源下载 {@link #downloadingOriginal}</LI>
 *     <LI>支持序列化和反序列化，方便在 {@code Intent} 中使用</LI>
 *     <LI>可以在 {@link com.tuisongbao.engine.chat.media.ChatVoicePlayer} 中直接播放语音类型的消息</LI>
 * </UL>
 */
public class ChatMessage {
    transient private final String TAG = "TSB" + ChatMessage.class.getSimpleName();
    /***
     * This value is not unique, it is the message's serial number in a conversation,
     * A different conversation may has a message which has a same messageId.
     */
    private long messageId;
    private ChatType type = ChatType.SingleChat;
    private String from;
    private String to;
    private ChatMessageContent content;
    private String createdAt;

    transient private ChatManager mChatManager;
    transient private Engine mEngine;
    transient private boolean downloadingThumbnail = false;
    transient private boolean downloadingOriginal = false;

    public ChatMessage() {
    }

    public static Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new ChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class,
                new ChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageContentSerializer.class,
                new ChatMessageContentSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageEventContent.TYPE.class, new ChatMessageEventTypeSerializer());

        return gsonBuilder.create();
    }

    /**
     * 将字符串反序列化为 ChatMessage
     *
     * @param engine        Engine 实例，用来确定 ChatMessage 的上下文
     * @param jsonString    合法的 JSON 格式 {@code String}
     * @return ChatMessage 实例
     */
    public static ChatMessage deserialize(Engine engine, String jsonString) {
        ChatMessage message = getSerializer().fromJson(jsonString, ChatMessage.class);
        message.mEngine = engine;
        message.mChatManager = engine.getChatManager();

        return message;
    }

    /**
     * 将实例序列化为 JSON 格式的 {@code String}，可用于在 {@code Intent} 之间直接传递该实例
     *
     * @return  JSON 格式的 {@code String}
     */
    public String serialize() {
        return getSerializer().toJson(this);
    }

    public void setEngine(Engine engine) {
        this.mEngine = engine;
        mChatManager = engine.getChatManager();
    }

    /**
     * 获取聊天类型
     *
     * @return 单聊或群聊
     */
    public ChatType getChatType() {
        return type;
    }

    /**
     * 设置聊天类型
     *
     * @param type 聊天类型
     * @return ChatMessage 实例
     */
    public ChatMessage setChatType(ChatType type) {
        this.type = type;
        return this;
    }

    /**
     * 获取自增消息 ID
     *
     * <P>
     *     每个 {@link com.tuisongbao.engine.chat.conversation.entity.ChatConversation} 都是从 0 开始。
     *
     * @return 自增消息 ID
     */
    public long getMessageId() {
        return messageId;
    }

    public ChatMessage setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * 获取消息发送者唯一标识
     *
     * @return 消息发发送者唯一标识
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置消息发送者唯一标识
     *
     * @param from 消息发送者唯一标识
     * @return
     */
    public ChatMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    /**
     * 获取消息接收者唯一标识
     *
     * @return 消息发接收者唯一标识
     */
    public String getRecipient() {
        return to;
    }

    /**
     * 设置消息接收者唯一标识
     *
     * @param to 消息接收者唯一标识
     * @return
     */
    public ChatMessage setRecipient(String to) {
        this.to = to;
        return this;
    }

    /**
     * 获取消息内容
     *
     * <P>
     *     ChatMessageContent 是父类，应根据 {@link ChatMessageContent#getType()} 来获取相应的内容。
     *
     * @return 消息内容
     */
    public ChatMessageContent getContent() {
        return content;
    }

    /**
     * 设置消息内容
     *
     * @param content 消息内容
     * @return ChatMessage 实例
     */
    public ChatMessage setContent(ChatMessageContent content) {
        this.content = content;
        return this;
    }

    /**
     * 获取消息创建时间，以服务器时间为准
     *
     * @return 创建时间，ISO8061 格式的字符串
     */
    public String getCreatedAt() {
        return createdAt;
    }

    public ChatMessage setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * 获取消息创建时间，以服务器时间为准
     *
     * @return 创建时间
     * @since v2.1.1
     */
    public Date getCreatedAtInDate() {
        if (createdAt == null) {
            return null;
        }
        return StrUtils.getDateFromTimeStringIOS8061(createdAt);
    }

    /**
     * 消息内容类型的枚举类
     */
    public enum TYPE {
        TEXT("text"),
        IMAGE("image"),
        VOICE("voice"),
        VIDEO("video"),
        EVENT("event"),
        LOCATION("location");

        private String name;

        TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static TYPE getType(String name) {
            if (!StrUtils.isEmpty(name)) {
                TYPE[] types = values();
                for (TYPE type : types) {
                    if (type.getName().equals(name)) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    /**
     * 下载图片并存储在本地
     *
     * <P>
     *     该方法<STRONG>可以</STRONG>重复调用 ，SDK 会自行检测图片路径是否有效，路径不存在时会重新下载。
     *
     * @param isOriginal {@code true} 表示下载原图; {@code false} 表示下载缩略图
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public void downloadImage(boolean isOriginal, final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        downloadResource(isOriginal, filePathCallback, progressCallback);
    }

    /**
     * 下载语音并存储在本地
     *
     * <P>
     *     可以直接使用 {@link com.tuisongbao.engine.chat.media.ChatVoicePlayer} 播放推送宝的语音消息，其中包含了下载。
     *
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public void downloadVoice(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        downloadResource(true, filePathCallback, progressCallback);
    }

    /**
     * 下载视频首帧缩略图
     *
     * <P>
     *     该方法<STRONG>可以</STRONG>重复调用 ，SDK 会自行检测图片路径是否有效，路径不存在时会重新下载。
     *
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public void downloadVideoThumb(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        downloadResource(false, filePathCallback, progressCallback);
    }

    /**
     * 下载视频
     *
     * <P>
     *     该方法<STRONG>可以</STRONG>重复调用 ，SDK 会自行检测图片路径是否有效，路径不存在时会重新下载。
     *
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public void downloadVideo(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        downloadResource(true, filePathCallback, progressCallback);
    }

    public boolean generateThumbnail(int maxWidth) {
        if (getContent().getType() != TYPE.IMAGE) {
            return false;
        }

        String thumbnailPath = getContent().getFile().getThumbnailPath();
        if (isFileExists(thumbnailPath)) {
            return false;
        }

        // Create thumbnail bitmap
        String filePath = getContent().getFile().getFilePath();
        Bitmap image = BitmapFactory.decodeFile(filePath);
        float bitmapRatio = (float)image.getWidth() / (float) image.getHeight();

        int width = Math.min(image.getWidth(), maxWidth);
        int height = (int) (width / bitmapRatio);
        Bitmap thumbnail = Bitmap.createScaledBitmap(image, width, height, true);

        // Save thumbnail
        String fileName = StrUtils.getTimestampStringOnlyContainNumber(new Date()) + ".jpg";
        FileOutputStream out = null;
        try {
            File file = DownloadUtils.getOutputFile(fileName, getContent().getType().getName());
            out = new FileOutputStream(file.getAbsolutePath());
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);

            // Update thumbnail path in message
            getContent().getFile().setThumbnailPath(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.error(TAG, e);
            }
        }
        return false;
    }

    private ResponseError permissionCheck() {
        if (!isMediaMessage()) {
            ResponseError error = new ResponseError();
            error.setMessage("No resource to download, this is not a media message.");
            return error;
        }
        return null;
    }

    private boolean isFileExists(String filePath) {
        if (StrUtils.isEmpty(filePath)) {
            return false;
        }
        File fileTest = new File(filePath);
        if (!fileTest.exists()) {
            LogUtil.warn(TAG, "Local file at " + filePath + " is no longer exists, need to download again");
            return false;
        }
        return true;
    }

    private void downloadResource(final boolean isOriginal, final EngineCallback callback, final ProgressCallback progressCallback) {
        ResponseError error = permissionCheck();
        if (error != null) {
            callback.onError(error);
            return;
        }

        String filePath;
        String downloadUrl;
        final boolean isDownloading;
        ChatMessageFileContent file = getContent().getFile();
        if (isOriginal) {
            filePath = file.getFilePath();
            downloadUrl = file.getUrl();
            isDownloading = downloadingOriginal;
        } else {
            filePath = file.getThumbnailPath();
            downloadUrl = file.getThumbUrl();
            isDownloading = downloadingThumbnail;
        }

        if (isDownloading) {
            return;
        }

        if (isFileExists(filePath)) {
            callback.onSuccess(filePath);
            return;
        }

        TYPE type = content.getType();
        // Download thumbnail of video
        if (content.getType() == TYPE.VIDEO && !isOriginal) {
            type = TYPE.IMAGE;
        }
        DownloadUtils.downloadResourceIntoLocal(downloadUrl, type, new EngineCallback<String>() {

            @Override
            public void onSuccess(String filePath) {
                updateFilePath(isOriginal, filePath);
                callback.onSuccess(filePath);
            }

            @Override
            public void onError(ResponseError error) {
                callback.onError(error);
            }
        }, progressCallback);
    }

    @Override
    public String toString() {
        return String.format("ChatMessage[messageId: %s, from: %s, to: %s, chatType: %s, content: %s, createdAt: %s]"
                , messageId, from, to, type.getName(), content.toString(), createdAt);
    }

    private void updateFilePath(boolean isOriginal, String filePath) {
        if (isOriginal) {
            downloadingOriginal = false;
            content.getFile().setFilePath(filePath);
        } else {
            downloadingThumbnail = false;
            content.getFile().setThumbnailPath(filePath);
        }
        if (mChatManager.isCacheEnabled()) {
            ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), mEngine);
            dataSource.open();
            dataSource.updateMessage(this);
            dataSource.close();
        }
    }

    private boolean isMediaMessage() {
        TYPE contentType = getContent().getType();
        return contentType == TYPE.IMAGE || contentType == TYPE.VOICE || contentType == TYPE.VIDEO;
    }
}
