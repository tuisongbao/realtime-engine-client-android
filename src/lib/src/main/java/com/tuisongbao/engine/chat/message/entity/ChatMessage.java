package com.tuisongbao.engine.chat.message.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.serializer.ChatMessageBodySerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.DownloadUtil;
import com.tuisongbao.engine.util.StrUtil;

import java.io.File;

public class ChatMessage implements Parcelable {
    transient private final String TAG = "TSB" + ChatMessage.class.getSimpleName();
    /***
     * This value is not unique, it is the message's serial number in a conversation,
     * A different conversation may has a message which has a same messageId.
     */
    private long messageId;
    private ChatType type = ChatType.SingleChat;
    private String from;
    private String to;
    private ChatMessageBody content;
    private String createdAt;
    private boolean downloading = false;

    transient private ChatManager mChatManager;
    transient private TSBEngine mEngine;

    public ChatMessage(TSBEngine engine) {
        mEngine = engine;
        mChatManager = mEngine.getChatManager();
    }

    public static Gson getSerializer() {
        // TODO: 15-8-2 Remove this after supporting deserialize in BaseEvent
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new ChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class,
                new ChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageBody.class,
                new ChatMessageBodySerializer());

        return gsonBuilder.create();
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

    public ChatMessageBody getBody() {
        return content;
    }

    public ChatMessage setBody(ChatMessageBody body) {
        this.content = body;
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
            ChatMessageBody body = getBody();
            if (isMediaMessage()) {
                return ((ChatMediaMessageBody)body).getLocalPath();
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
        return "";
    }

    public String getText() {
        try {
            ChatMessageBody body = getBody();
            if (body.getType() == TYPE.TEXT) {
                return ((ChatTextMessageBody)body).getText();
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
            if (!StrUtil.isEmpty(name)) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(messageId);
        dest.writeString(type == null ? "" : type.getName());
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(createdAt);
        dest.writeParcelable(content, flags);
    }

    public void readFromParcel(Parcel in) {
        messageId = in.readLong();
        String nameType = in.readString();
        type = StrUtil.isEmpty(nameType) ? null : ChatType.getType(nameType);
        from = in.readString();
        to = in.readString();
        createdAt = in.readString();
        content = in.readParcelable(ChatMessageBody.class.getClassLoader());
    }

    public static final Parcelable.Creator<ChatMessage> CREATOR =
            new Parcelable.Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    private ChatMessage(Parcel in) {
        readFromParcel(in);
    }

    public void downloadResource(final TSBEngineCallback<ChatMessage> callback, final TSBProgressCallback progressCallback) {
        final ChatConversationDataSource dataSource = new ChatConversationDataSource(TSBEngine.getContext(), mEngine);
        final ChatMessage message = this;

        if (getBody().getType() == TYPE.TEXT) {
            LogUtil.warn(TAG, "Message type is text!");
            return;
        }

        if (downloading) {
            LogUtil.warn(TAG, "Downloading is in progress");
            return;
        }

        final ChatMediaMessageBody body = (ChatMediaMessageBody)getBody();
        String localPath = body.getLocalPath();
        String downloadUrl = body.getDownloadUrl();
        try {
            boolean needDownload = StrUtil.isEmpty(localPath);
            if (!needDownload) {
                File fileTest = new File(localPath);
                if (!fileTest.exists()) {
                    needDownload = true;
                    LogUtil.verbose(TAG, "Local file at " + localPath + " is no longer exists, need to download again" );
                }
            }

            if (needDownload) {
                downloading = true;
                DownloadUtil.downloadResourceIntoLocal(downloadUrl, body.getType(), new TSBEngineCallback<String>() {

                    @Override
                    public void onSuccess(String filePath) {
                        downloading = false;
                        body.setLocalPath(filePath);

                        // TODO: Get cache status and update DB
                        if (mChatManager.isCacheEnabled()) {
                            dataSource.open();
                            dataSource.upsertMessage(mChatManager.getChatUser().getUserId(), message);
                            LogUtil.verbose(TAG, "Update message local path " + message);
                            dataSource.close();
                        }
                        message.setBody(body);
                        callback.onSuccess(message);
                    }

                    @Override
                    public void onError(ResponseError error) {
                        downloading = false;
                        callback.onError(error);
                    }
                }, progressCallback);
            } else {
                callback.onSuccess(message);
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            callback.onError(mEngine.getUnhandledResponseError());
        }
    }

    @Override
    public String toString() {
        return String.format("ChatMessage[messageId: %s, from: %s, to: %s, chatType: %s, content: %s, createdAt: %s]"
                , messageId, from, to, type.getName(), content.toString(), createdAt);
    }

    private boolean isMediaMessage() {
        TYPE bodyType = getBody().getType();
        return bodyType == TYPE.IMAGE || bodyType == TYPE.VOICE || bodyType == TYPE.VIDEO;
    }
}
