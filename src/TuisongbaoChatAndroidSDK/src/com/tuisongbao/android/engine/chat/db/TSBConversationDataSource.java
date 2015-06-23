package com.tuisongbao.android.engine.chat.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBImageMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMediaMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBTextMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBVoiceMessageBody;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBConversationDataSource {
    private static final String TABLE_CONVERSATION = TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION;
    private static final String TABLE_MESSAGE = TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE;

    private SQLiteDatabase conversationDB;
    private SQLiteDatabase messageDB;
    private TSBConversationSQLiteHelper conversationSQLiteHelper;
    private TSBMessageSQLiteHelper messageSQLiteHelper;

    public TSBConversationDataSource(Context context) {
        conversationSQLiteHelper = new TSBConversationSQLiteHelper(context);
        messageSQLiteHelper = new TSBMessageSQLiteHelper(context);
    }

    public void open() {
        conversationDB = conversationSQLiteHelper.getWritableDatabase();
        messageDB = messageSQLiteHelper.getWritableDatabase();
    }

    public void close() {
        conversationSQLiteHelper.close();
        messageSQLiteHelper.close();
    }

    public String getLatestLastActiveAt(String userId) {
        String sql = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + TSBConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'"
                + " ORDER BY datetime(" + TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT + ") DESC LIMIT 1";
        Cursor cursor = conversationDB.rawQuery(sql, null);
        if (cursor.isAfterLast()) {
            return null;
        }
        cursor.moveToFirst();
        return cursor.getString(5);
    }

    /***
     * Try to update items, if not rows effected then insert.
     *
     * @param conversation
     * @param userId
     */
    public void upsert(TSBChatConversation conversation, String userId) {
        int rowsEffected = update(conversation, userId);
        if (rowsEffected < 1) {
            insert(conversation, userId);
        }
    }

    public void upsert(List<TSBChatConversation> conversations, String userId) {
        for (TSBChatConversation conversation : conversations) {
            upsert(conversation, userId);
        }
    }

    public List<TSBChatConversation> getList(String userId, ChatType type, String target) {
        String queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + TSBConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        Cursor cursor = null;
        List<TSBChatConversation> conversations = new ArrayList<TSBChatConversation>();

        if(!StrUtil.isEmpty(target)) {
            queryString = queryString
                    + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = '" + target + "'";
        } else if (StrUtil.isEmpty(target) && type != null) {
            queryString = queryString
                    + " AND " + TSBConversationSQLiteHelper.COLUMN_TYPE + " = '" + type.getName() + "'";
        }
        queryString = queryString + ";";
        cursor = conversationDB.rawQuery(queryString, null);
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " conversations by user "
                + userId + " and target " + target);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBChatConversation conversation = createConversation(cursor);
            conversations.add(conversation);
            cursor.moveToNext();
        }
        cursor.close();

        return conversations;
    }

    /**
     * When new message received or first message of chat, there is no such conversation of message's from and to,
     * so it will create a conversation for it.
     *
     * @param message
     */
    public void upsertMessage(String userId, final TSBMessage message) {
        // TODO: transaction
        if (updateMessage(message) > 0) {
            return;
        }

        // Create conversation if no such conversation between from and to
        String target = message.getRecipient();
        if (message.getChatType() == ChatType.SingleChat) {
            if (StrUtil.isEqual(target, userId)) {
                target = message.getFrom();
            }
        }
        boolean needCreateConversation = !isConversationExist(userId, target);
        if (needCreateConversation) {
            TSBChatConversation conversation = new TSBChatConversation();
            conversation.setTarget(target);
            conversation.setType(message.getChatType());
            conversation.setUnreadMessageCount(1);
            insert(conversation, userId);
        }
        if (insertMessage(message) > 0) {
            LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, "insert " + message);
        }
    }


    /**
     * Conversation's type and target map the message's type and to field relatively.
     *
     * @param type
     * @param target
     * @return List<TSBMessage>
     */
    public List<TSBMessage> getMessages(String userId, ChatType type, String target, Long startMessageId, Long endMessageId, int limit) {
        List<TSBMessage> messages = new ArrayList<TSBMessage>();
        Cursor cursor = null;
        String queryString = "";
        if (type == ChatType.GroupChat) {
            queryString = "SELECT * FROM " + TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                    + " WHERE " + TSBMessageSQLiteHelper.COLUMN_CHAT_TYPE + " = '" + type.getName() + "'"
                    + " AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = '" + target + "'";
        } else {
            queryString = "SELECT * FROM " + TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                    + " WHERE " + TSBMessageSQLiteHelper.COLUMN_CHAT_TYPE + " = '" + type.getName() + "'"
                    + " AND "
                    + "((" + TSBMessageSQLiteHelper.COLUMN_FROM + " = '" + userId + "' AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = '" + target
                    + "') OR "
                    + "(" + TSBMessageSQLiteHelper.COLUMN_FROM + " = '" + target + "' AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = '" + userId
                    + "'))";
        }
        if (startMessageId != null) {
            queryString += " AND " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " <= " + startMessageId;
        }
        if (endMessageId != null) {
            queryString = queryString + " AND " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " >= " + endMessageId;
        }
        queryString = queryString
                + " ORDER BY " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " DESC "
                + " LIMIT " + limit
                + ";";
        cursor = messageDB.rawQuery(queryString, null);
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " messages between "
                + userId + " and " + target);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBMessage message = createMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();

        return messages;
    }

    public void resetUnread(String userId, ChatType type, String target) {
        String whereClause = TSBConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TYPE + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = ?";
        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, 0);
        int rowsAffected = conversationDB.update(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION,
                values, whereClause, new String[]{ userId, type.getName(), target });
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, rowsAffected + " rows affected when reset unread count between " + userId + " and " + target);
    }

    public void remove(String userId, ChatType type, String target) {
        String whereClause = TSBConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TYPE + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = ?";
        int rowsAffected = conversationDB.delete(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, whereClause,
                new String[]{ userId, type.getName(), target });
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Remove conversation:[type: " + type.getName() + ", target: " + target + "]"
                + " and " + rowsAffected + " rows affected");

        removeMessages(userId, type, target);
    }

    /***
     * Only used for updating the localpath field
     *
     * @param message
     * @return the rows effected
     */
    public int updateMessage(TSBMessage message) {
        String uniqueMessageId = generateUniqueMessageId(message);
        String whereClause = TSBMessageSQLiteHelper.COLUMN_ID + " = ?";

        ContentValues values = new ContentValues();
        TSBMessageBody body = message.getBody();
        if (body != null && isMediaMessage(message)) {
            TSBMediaMessageBody mediaBody = (TSBMediaMessageBody)body;
            String localPath = mediaBody.getLocalPath();
            if (!StrUtil.isEmpty(localPath)) {
                values.put(TSBMessageSQLiteHelper.COLUMN_FILE_LOCAL_PATH, localPath);
            }
        }

        values.put(TSBMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        return messageDB.update(TABLE_MESSAGE, values, whereClause, new String[]{ uniqueMessageId });
    }

    private void insert(TSBChatConversation conversation, String userId) {
        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_USER_ID, userId);
        values.put(TSBConversationSQLiteHelper.COLUMN_TARGET, conversation.getTarget());
        values.put(TSBConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());
        values.put(TSBConversationSQLiteHelper.COLUMN_GROUP_NAME, conversation.getGroupName());

        long id = conversationDB.insert(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, null, values);
        LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, "insert " + conversation + " with return id " + id);
    }

    private void removeMessages(String userId, ChatType type, String target) {
        String whereClause = "";
        int rowsAffected = 0;
        if (type == ChatType.GroupChat) {
            whereClause = TSBMessageSQLiteHelper.COLUMN_TO + " = ?";
            rowsAffected = messageDB.delete(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, whereClause,
                    new String[]{ target});
        } else {
            whereClause = "(" + TSBMessageSQLiteHelper.COLUMN_FROM + " = ?" + " AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = ?)"
                    + " OR (" +  TSBMessageSQLiteHelper.COLUMN_FROM + " = ?" + " AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = ?)";
            rowsAffected = messageDB.delete(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, whereClause,
                    new String[]{ userId, target, target, userId });
        }
        LogUtil.info(LogUtil.LOG_TAG_CHAT_CACHE, "Removed " + rowsAffected + " messages between " + userId + " and " + target);
    }

    /***
     *
     * @param conversation
     * @param userId
     * @return rows effected
     */
    private int update(TSBChatConversation conversation, String userId) {
        String currentUserId = TSBChatManager.getInstance().getChatUser().getUserId();
        String whereClause = TSBConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = ?";

        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());
        values.put(TSBConversationSQLiteHelper.COLUMN_GROUP_NAME, conversation.getGroupName());

        int rowsAffected = conversationDB.update(TABLE_CONVERSATION, values, whereClause,
                new String[]{ currentUserId, conversation.getTarget() });
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Update " + conversation + " and " + rowsAffected + " rows affected");
        return rowsAffected;
    }

    private TSBChatConversation createConversation(Cursor cursor) {
        TSBChatConversation conversation = new TSBChatConversation();
        conversation.setTarget(cursor.getString(2));
        conversation.setType(ChatType.getType(cursor.getString(3)));
        conversation.setUnreadMessageCount(cursor.getInt(4));
        conversation.setLastActiveAt(cursor.getString(5));
        conversation.setGroupName(cursor.getString(6));

        return conversation;
    }

    private TSBMessage createMessage(Cursor cursor) {
        TSBMessage message = new TSBMessage();
        message.setMessageId(cursor.getLong(1));
        message.setFrom(cursor.getString(2));
        message.setRecipient(cursor.getString(3));
        message.setChatType(ChatType.getType(cursor.getString(4)));

        String contentType = cursor.getString(6);
        TSBMessageBody body = null;
        if (StrUtil.isEqual(TYPE.TEXT.getName(), contentType)) {
            TSBTextMessageBody textBody = new TSBTextMessageBody();
            textBody.setText(cursor.getString(5));

            body = textBody;
        } else {
            TSBMediaMessageBody mediaBody = null;
            if (StrUtil.isEqual(TYPE.IMAGE.getName(), contentType)) {
                mediaBody = new TSBImageMessageBody();
            } else if (StrUtil.isEqual(TYPE.VOICE.getName(), contentType)) {
                mediaBody = new TSBVoiceMessageBody();
            }

            mediaBody.setLocalPath(cursor.getString(7));
            mediaBody.setDownloadUrl(cursor.getString(8));
            mediaBody.setSize(cursor.getString(9));
            mediaBody.setMimeType(cursor.getString(10));

            mediaBody.setWidth(cursor.getInt(11));
            mediaBody.setHeight(cursor.getInt(12));
            mediaBody.setDuration(cursor.getString(13));

            body = mediaBody;
        }
        message.setBody(body);
        message.setCreatedAt(cursor.getString(14));

        return message;
    }

    private String generateUniqueMessageId(TSBMessage message) {
        // These three value can unique specified a message
        return message.getFrom() + "#" + message.getRecipient() + "#" + message.getMessageId();
    }

    private boolean isConversationExist(String userId, String target) {
        if (StrUtil.isEqual(userId,  target)) {
            return true;
        }

        String queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + TSBConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = '" + target + "'"
                + ";";
        Cursor cursor = conversationDB.rawQuery(queryString, null);
        return cursor.getCount() > 0;
    }

    private long insertMessage(TSBMessage message) {
        ContentValues values =  new ContentValues();
        values.put(TSBMessageSQLiteHelper.COLUMN_ID, generateUniqueMessageId(message));
        values.put(TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(TSBMessageSQLiteHelper.COLUMN_FROM, message.getFrom());
        values.put(TSBMessageSQLiteHelper.COLUMN_TO, message.getRecipient());
        values.put(TSBMessageSQLiteHelper.COLUMN_CHAT_TYPE, message.getChatType().getName());
        values.put(TSBMessageSQLiteHelper.COLUMN_CONTENT_TYPE, message.getBody().getType().getName());

        if (message.getBody().getType() == TYPE.TEXT) {
            TSBTextMessageBody textMessageBody = (TSBTextMessageBody)message.getBody();
            values.put(TSBMessageSQLiteHelper.COLUMN_CONTENT, textMessageBody.getText());
        } else if (isMediaMessage(message)) {
            TSBMediaMessageBody mediaBody = (TSBMediaMessageBody)message.getBody();

            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_LOCAL_PATH, mediaBody.getLocalPath());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_DOWNLOAD_URL, mediaBody.getDownloadUrl());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_SIZE, mediaBody.getSize());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_MIMETYPE, mediaBody.getMimeType());

            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_WIDTH, mediaBody.getWidth());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_HEIGHT, mediaBody.getHeight());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_DURATION, mediaBody.getDuration());
        }

        values.put(TSBMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        return messageDB.insert(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, null, values);
    }

    private boolean isMediaMessage(TSBMessage message) {
        TYPE type = message.getBody().getType();
        return type  == TYPE.IMAGE || type == TYPE.VOICE;
    }
}
