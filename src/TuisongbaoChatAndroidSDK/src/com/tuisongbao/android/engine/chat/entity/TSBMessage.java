package com.tuisongbao.android.engine.chat.entity;

import java.io.File;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.DownloadUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBMessage implements Parcelable {
    /***
     * This value is not unique, it is the message's order number in a conversation,
     * A different conversation may has a message which has a same messageId.
     */
    private long messageId;
    private ChatType type = ChatType.SingleChat;
    private String from;
    private String to;
    private TSBMessageBody content;
    private String createdAt;
    private boolean downloading = false;

    public ChatType getChatType() {
        return type;
    }

    public TSBMessage setChatType(ChatType type) {
        this.type = type;
        return this;
    }

    public long getMessageId() {
        return messageId;
    }

    public TSBMessage setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public TSBMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getRecipient() {
        return to;
    }

    public TSBMessage setRecipient(String to) {
        this.to = to;
        return this;
    }

    public TSBMessageBody getBody() {
        return content;
    }

    public TSBMessage setBody(TSBMessageBody body) {
        this.content = body;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public TSBMessage setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /***
     *
     * @return local path of the resource, like image, video...
     */
    public String getResourcePath() {
        try {
            TSBMessageBody body = getBody();
            if (isMediaMessage()) {
                return ((TSBMediaMessageBody)body).getLocalPath();
            }
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }
        return "";
    }

    public String getText() {
        try {
            TSBMessageBody body = getBody();
            if (body.getType() == TYPE.TEXT) {
                return ((TSBTextMessageBody)body).getText();
            }
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }
        return "";
    }

    public static TSBMessage createMessage(TYPE type) {
        TSBMessage message = new TSBMessage();
        message.setBody(TSBMessageBody.createMessage(type));
        return message;
    }

    public static enum TYPE {
        TEXT("text", 1),
        IMAGE("image", 2),
        VOICE("voice", 3);

        private String name;
        private int index;

        private TYPE(String name, int index) {
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
        content = in.readParcelable(TSBMessageBody.class.getClassLoader());
    }

    public static final Parcelable.Creator<TSBMessage> CREATOR =
            new Parcelable.Creator<TSBMessage>() {
        @Override
        public TSBMessage createFromParcel(Parcel in) {
            return new TSBMessage(in);
        }

        @Override
        public TSBMessage[] newArray(int size) {
            return new TSBMessage[size];
        }
    };

    private TSBMessage(Parcel in) {
        readFromParcel(in);
    }

    public TSBMessage() {
        // empty
    }

    public void downloadResource(final TSBEngineCallback<TSBMessage> callback) {
        final TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        final String userId = TSBChatManager.getInstance().getChatUser().getUserId();
        final TSBMessage message = this;

        if (getBody().getType() == TYPE.TEXT) {
            callback.onError(EngineConstants.ENGINE_CODE_INVALID_OPERATION, "Text message has no resource to download");
            return;
        }

        if (downloading) {
            callback.onError(EngineConstants.ENGINE_CODE_INVALID_OPERATION, "Downloading is in process!");
            return;
        }

        final TSBMediaMessageBody body = (TSBMediaMessageBody)getBody();
        String localPath = body.getLocalPath();
        String downloadUrl = body.getDownloadUrl();
        try {
            boolean needDownload = StrUtil.isEmpty(localPath);
            if (!needDownload) {
                File fileTest = new File(localPath);
                if (!fileTest.exists()) {
                    needDownload = true;
                    LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Local file at " + localPath + " is no longer exists, need to download again" );
                }
            }

            if (needDownload) {
                downloading = true;
                String fileName = StrUtil.getTimestampStringOnlyContainNumber(new Date());
                DownloadUtil.downloadResourceIntoLocal(downloadUrl, fileName, body.getType().getName(), new TSBEngineCallback<String>() {

                    @Override
                    public void onSuccess(String t) {
                        downloading = false;
                        body.setLocalPath(t);
                        dataSource.open();
                        dataSource.upsertMessage(userId, message);
                        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Update message local path " + message);
                        dataSource.close();

                        message.setBody(body);
                        callback.onSuccess(message);
                    }

                    @Override
                    public void onError(int code, String message) {
                        downloading = false;
                        callback.onError(code, message);
                    }
                });
            } else {
                callback.onSuccess(message);
            }
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
            callback.onError(EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
        }
    }

    @Override
    public String toString() {
        return String.format("TSBMessage[messageId: %s, from: %s, to: %s, chatType: %s, content: %s, createdAt: %s]"
                , messageId, from, to, type.getName(), content.toString(), createdAt);
    }

    private boolean isMediaMessage() {
        TYPE bodyType = getBody().getType();
        return bodyType == TYPE.IMAGE || bodyType == TYPE.VOICE;
    }
}
