package com.tuisongbao.engine.chat.message.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileContent;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageContentSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.DownloadUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

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
    transient private TSBEngine mEngine;
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

        return gsonBuilder.create();
    }

    public static ChatMessage deserialize(TSBEngine engine, String jsonString) {
        ChatMessage message = getSerializer().fromJson(jsonString, ChatMessage.class);
        message.mEngine = engine;
        message.mChatManager = engine.getChatManager();

        return message;
    }

    public String serialize() {
        return getSerializer().toJson(this);
    }

    public void setEngine(TSBEngine engine) {
        this.mEngine = engine;
        mChatManager = engine.getChatManager();
    }

    public ChatType getChatType() {
        return type;
    }

    public ChatMessage setChatType(ChatType type) {
        this.type = type;
        return this;
    }

    public long getMessageId() {
        return messageId;
    }

    public ChatMessage setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public ChatMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getRecipient() {
        return to;
    }

    public ChatMessage setRecipient(String to) {
        this.to = to;
        return this;
    }

    public ChatMessageContent getContent() {
        return content;
    }

    public ChatMessage setContent(ChatMessageContent content) {
        this.content = content;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ChatMessage setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /***
     *
     * @return local path of the resource, like image, video...
     */
    public String getResourcePath() {
        try {
            if (isMediaMessage()) {
                ChatMessageContent content = getContent();
                return content.getFile().getFilePath();
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
        return "";
    }

    public enum TYPE {
        TEXT("text", 1),
        IMAGE("image", 2),
        VOICE("voice", 3),
        VIDEO("video", 4),
        EVENT("event", 5);

        private String name;
        private int index;

        TYPE(String name, int index) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
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

    public void downloadImage(boolean isOriginal, final TSBEngineCallback<String> callback, final TSBProgressCallback progressCallback) {
        downloadResource(isOriginal, callback, progressCallback);
    }

    public void downloadVoice(final TSBEngineCallback callback, final TSBProgressCallback progressCallback) {
        downloadResource(true, callback, progressCallback);
    }

    public void downloadVideoThumb(final TSBEngineCallback callback, final TSBProgressCallback progressCallback) {
        downloadResource(false, callback, progressCallback);
    }

    public void downloadVideo(final TSBEngineCallback callback, final TSBProgressCallback progressCallback) {
        downloadResource(true, callback, progressCallback);
    }

    public void generateThumbnail(int maxWidth) {
        if (getContent().getType() != TYPE.IMAGE) {
            return;
        }

        String thumbnailPath = getContent().getFile().getThumbnailPath();
        if (isFileExists(thumbnailPath)) {
            return;
        }

        // Create thumbnail bitmap
        String filePath = getContent().getFile().getFilePath();
        Bitmap image = BitmapFactory.decodeFile(filePath);
        int width = Math.min(image.getWidth(), maxWidth);
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        height = (int) (width / bitmapRatio);
        Bitmap thumbnail = Bitmap.createScaledBitmap(image, width, height, true);

        // Save thumbnail
        String fileName = StrUtils.getTimestampStringOnlyContainNumber(new Date()) + ".jpg";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);

            // Update thumbnail path in message
            getContent().getFile().setThumbnailPath(fileName);
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

    private void downloadResource(final boolean isOriginal, final TSBEngineCallback callback, final TSBProgressCallback progressCallback) {
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
        DownloadUtils.downloadResourceIntoLocal(downloadUrl, type, new TSBEngineCallback<String>() {

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
            ChatConversationDataSource dataSource = new ChatConversationDataSource(TSBEngine.getContext(), mEngine);
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
