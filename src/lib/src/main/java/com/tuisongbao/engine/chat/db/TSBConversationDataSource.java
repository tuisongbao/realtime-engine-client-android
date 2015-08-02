package com.tuisongbao.engine.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.event.event.ChatEventMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatImageMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMediaMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatTextMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatVideoMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatVoiceMessageBody;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TSBConversationDataSource {
    private static final String TAG = "com.tuisongbao.engine.TSBConversationDataSource";
    private static final String TABLE_CONVERSATION = TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION;
    private static final String TABLE_MESSAGE = TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE;

    private SQLiteDatabase conversationDB;
    private SQLiteDatabase messageDB;
    private TSBConversationSQLiteHelper conversationSQLiteHelper;
    private TSBMessageSQLiteHelper messageSQLiteHelper;
    private TSBEngine mEngine;

    public TSBConversationDataSource(Context context, TSBEngine engine) {
        mEngine = engine;
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
     * Try to update items, if no rows effected then insert.
     * include inserting the lastMessage of conversation.
     *
     * @param conversation
     * @param userId
     */
    public void upsert(ChatConversation conversation, String userId) {
        int rowsEffected = update(conversation, userId);
        if (rowsEffected < 1) {
            insert(conversation, userId);
        }
        upsertMessage(userId, conversation.getLastMessage());
    }

    /***
     * Update or insert conversation, include it's lastMessage.
     *
     * @param conversations
     * @param userId
     */
    public void upsert(List<ChatConversation> conversations, String userId) {
        for (ChatConversation conversation : conversations) {
            upsert(conversation, userId);
        }
    }

    public List<ChatConversation> getList(String userId, ChatType type, String target) {
        String queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + TSBConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        Cursor cursor;
        List<ChatConversation> conversations = new ArrayList<ChatConversation>();

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
            ChatConversation conversation = createConversation(cursor);
            conversation.setLastMessage(getLastMessage(userId, conversation.getType(), conversation.getTarget()));
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
    public void upsertMessage(String userId, final ChatMessage message) {
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
            ChatConversation conversation = new ChatConversation(mEngine);
            conversation.setTarget(target);
            conversation.setType(message.getChatType());
            conversation.setUnreadMessageCount(1);
            insert(conversation, userId);
        }
        if (insertMessage(message) > 0) {
            LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, "insert " + message);
        }
    }

    public ChatMessage getLastMessage(String userId, ChatType type, String target) {
        ChatMessage lastMessage = null;
        String query = generateQueryBy(userId, type, target);
        query = query
                + " ORDER BY " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " DESC "
                + " LIMIT " + 1
                + ";";
        Cursor cursor = messageDB.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            lastMessage = createMessage(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return  lastMessage;
    }


    /**
     * Conversation's type and target map the message's type and to field relatively.
     *
     * @param type
     * @param target
     * @return List<ChatMessage>
     */
    public List<ChatMessage> getMessages(String userId, ChatType type, String target, Long startMessageId, Long endMessageId, int limit) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        Cursor cursor;
        String query= generateQueryBy(userId, type, target);
        if (startMessageId != null) {
            query += " AND " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " <= " + startMessageId;
        }
        if (endMessageId != null) {
            query = query + " AND " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " >= " + endMessageId;
        }
        query = query
                + " ORDER BY " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " DESC "
                + " LIMIT " + limit
                + ";";
        cursor = messageDB.rawQuery(query, null);
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " messages between "
                + userId + " and " + target);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatMessage message = createMessage(cursor);
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
    public int updateMessage(ChatMessage message) {
        String uniqueMessageId = generateUniqueMessageId(message);
        String whereClause = TSBMessageSQLiteHelper.COLUMN_ID + " = ?";

        ContentValues values = new ContentValues();
        ChatMessageBody body = message.getBody();
        if (body != null && isMediaMessage(message)) {
            ChatMediaMessageBody mediaBody = (ChatMediaMessageBody)body;
            String localPath = mediaBody.getLocalPath();
            if (!StrUtil.isEmpty(localPath)) {
                values.put(TSBMessageSQLiteHelper.COLUMN_FILE_LOCAL_PATH, localPath);
            }
        }

        values.put(TSBMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        return messageDB.update(TABLE_MESSAGE, values, whereClause, new String[]{ uniqueMessageId });
    }

    public void deleteAllData() {
        open();
        conversationDB.delete(TABLE_CONVERSATION, null, null);
        messageDB.delete(TABLE_MESSAGE, null, null);
    }

    private void insert(ChatConversation conversation, String userId) {
        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_USER_ID, userId);
        values.put(TSBConversationSQLiteHelper.COLUMN_TARGET, conversation.getTarget());
        values.put(TSBConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());

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
     * Update each field of conversation except *lastMessage*
     *
     * @param conversation
     * @param userId
     * @return rows effected
     */
    private int update(ChatConversation conversation, String userId) {
        String whereClause = TSBConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = ?";

        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());

        int rowsAffected = conversationDB.update(TABLE_CONVERSATION, values, whereClause,
                new String[]{ userId, conversation.getTarget() });
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Update " + conversation + " and " + rowsAffected + " rows affected");
        return rowsAffected;
    }

    private ChatConversation createConversation(Cursor cursor) {
        ChatConversation conversation = new ChatConversation(mEngine);
        conversation.setTarget(cursor.getString(2));
        conversation.setType(ChatType.getType(cursor.getString(3)));
        conversation.setUnreadMessageCount(cursor.getInt(4));
        conversation.setLastActiveAt(cursor.getString(5));

        return conversation;
    }

    private ChatMessage createMessage(Cursor cursor) {
        ChatMessage message = new ChatMessage(mEngine);
        message.setMessageId(cursor.getLong(1));
        message.setFrom(cursor.getString(2));
        message.setRecipient(cursor.getString(3));
        message.setChatType(ChatType.getType(cursor.getString(4)));

        String contentType = cursor.getString(6);
        ChatMessageBody body = null;
        if (StrUtil.isEqual(TYPE.TEXT.getName(), contentType)) {
            ChatTextMessageBody textBody = new ChatTextMessageBody();
            textBody.setText(cursor.getString(5));

            body = textBody;
        } else if (StrUtil.isEqual(TYPE.EVENT.getName(), contentType)) {
            ChatEventMessageBody eventBody = new ChatEventMessageBody();
            JsonObject event = new JsonObject();
            event.addProperty(ChatEventMessageBody.EVENT_TYPE, cursor.getString(14));
            event.addProperty(ChatEventMessageBody.EVENT_TARGET, cursor.getString(15));
            eventBody.setEvent(event);

            body = eventBody;
        } else {
            ChatMediaMessageBody mediaBody = null;
            if (StrUtil.isEqual(TYPE.IMAGE.getName(), contentType)) {
                ChatImageMessageBody imageBody = new ChatImageMessageBody();
                imageBody.setWidth(cursor.getInt(11));
                imageBody.setHeight(cursor.getInt(12));
                mediaBody = imageBody;
            } else if (StrUtil.isEqual(TYPE.VOICE.getName(), contentType)) {
                ChatVoiceMessageBody voiceBody = new ChatVoiceMessageBody();
                voiceBody.setDuration(cursor.getString(13));
                mediaBody = voiceBody;
            } else if (StrUtil.isEqual(TYPE.VIDEO.getName(), contentType)) {
                ChatVideoMessageBody videoBody = new ChatVideoMessageBody();
                videoBody.setDuration(cursor.getString(13));
                mediaBody = videoBody;
            }
            mediaBody.setLocalPath(cursor.getString(7));
            mediaBody.setDownloadUrl(cursor.getString(8));
            mediaBody.setSize(cursor.getString(9));
            mediaBody.setMimeType(cursor.getString(10));

            body = mediaBody;
        }

        String extraString = cursor.getString(16);
        if (extraString != null && extraString.length() > 0) {
            try {
                JsonParser parser = new JsonParser();
                JsonObject extraInJson = (JsonObject)parser.parse(extraString);
                body.setExtra(extraInJson);
            } catch (Exception e) {
                LogUtil.error(TAG, e);
            }
        }
        message.setBody(body);
        message.setCreatedAt(cursor.getString(17));

        return message;
    }

    private String generateUniqueMessageId(ChatMessage message) {
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

    private long insertMessage(ChatMessage message) {
        ContentValues values =  new ContentValues();
        values.put(TSBMessageSQLiteHelper.COLUMN_ID, generateUniqueMessageId(message));
        values.put(TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(TSBMessageSQLiteHelper.COLUMN_FROM, message.getFrom());
        values.put(TSBMessageSQLiteHelper.COLUMN_TO, message.getRecipient());
        values.put(TSBMessageSQLiteHelper.COLUMN_CHAT_TYPE, message.getChatType().getName());
        values.put(TSBMessageSQLiteHelper.COLUMN_CONTENT_TYPE, message.getBody().getType().getName());

        if (message.getBody().getType() == TYPE.TEXT) {
            ChatTextMessageBody textMessageBody = (ChatTextMessageBody)message.getBody();
            values.put(TSBMessageSQLiteHelper.COLUMN_CONTENT, textMessageBody.getText());
        } else if (isMediaMessage(message)) {
            ChatMediaMessageBody mediaBody = (ChatMediaMessageBody)message.getBody();

            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_LOCAL_PATH, mediaBody.getLocalPath());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_DOWNLOAD_URL, mediaBody.getDownloadUrl());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_SIZE, mediaBody.getSize());
            values.put(TSBMessageSQLiteHelper.COLUMN_FILE_MIMETYPE, mediaBody.getMimeType());

            if (mediaBody instanceof ChatImageMessageBody) {
                ChatImageMessageBody imageBody = (ChatImageMessageBody)mediaBody;
                values.put(TSBMessageSQLiteHelper.COLUMN_FILE_WIDTH, imageBody.getWidth());
                values.put(TSBMessageSQLiteHelper.COLUMN_FILE_HEIGHT, imageBody.getHeight());
            } else if (mediaBody instanceof ChatVoiceMessageBody) {
                ChatVoiceMessageBody voiceBody = (ChatVoiceMessageBody)mediaBody;
                values.put(TSBMessageSQLiteHelper.COLUMN_FILE_DURATION, voiceBody.getDuration());
            } else if (mediaBody instanceof ChatVideoMessageBody) {
                ChatVideoMessageBody videoBody = (ChatVideoMessageBody)mediaBody;
                values.put(TSBMessageSQLiteHelper.COLUMN_FILE_DURATION, videoBody.getDuration());
            }

        } else if (message.getBody().getType() == TYPE.EVENT) {
            ChatEventMessageBody eventBody = (ChatEventMessageBody)message.getBody();
            values.put(TSBMessageSQLiteHelper.COLUMN_EVENT_TYPE, eventBody.getEventType().getName());
            values.put(TSBMessageSQLiteHelper.COLUMN_EVENT_TARGET, eventBody.getEventTarget());
        }

        JSONObject extra = message.getBody().getExtra();
        LogUtil.debug(TAG, extra + "");
        if (extra != null) {
            values.put(TSBMessageSQLiteHelper.COLUMN_EXTRA, extra.toString());
        }

        values.put(TSBMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        return messageDB.insert(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, null, values);
    }

    private boolean isMediaMessage(ChatMessage message) {
        TYPE type = message.getBody().getType();
        return type  == TYPE.IMAGE || type == TYPE.VOICE || type == TYPE.VIDEO;
    }


    /***
     * The query generation is so tedious, use this to make it simple. No space at the end of the query,
     * so if there is more condition, do not forget to add space.
     *
     * @param userId
     * @param type
     * @param target
     * @return
     */
    private String generateQueryBy(String userId, ChatType type, String target) {
        String queryString;
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
        return queryString;
    }
}
