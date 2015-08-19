package com.tuisongbao.engine.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.message.content.ChatMessageLocationContent;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventEntity;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileEntity;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageLocationEntity;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationDataSource {
    private static final String TAG = "TSB" + ChatConversationDataSource.class.getSimpleName();
    private static final String TABLE_CONVERSATION = ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION;
    private static final String TABLE_MESSAGE = ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE;

    private SQLiteDatabase conversationDB;
    private SQLiteDatabase messageDB;
    private ChatConversationSQLiteHelper conversationSQLiteHelper;
    private ChatMessageSQLiteHelper messageSQLiteHelper;
    private Engine mEngine;

    public ChatConversationDataSource(Context context, Engine engine) {
        mEngine = engine;
        conversationSQLiteHelper = new ChatConversationSQLiteHelper(context);
        messageSQLiteHelper = new ChatMessageSQLiteHelper(context);
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
        String sql = "SELECT * FROM " + ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + ChatConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'"
                + " ORDER BY datetime(" + ChatConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT + ") DESC LIMIT 1";
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
        String queryString = "SELECT * FROM " + ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + ChatConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        Cursor cursor;
        List<ChatConversation> conversations = new ArrayList<ChatConversation>();

        if(!StrUtils.isEmpty(target)) {
            queryString = queryString
                    + " AND " + ChatConversationSQLiteHelper.COLUMN_TARGET + " = '" + target + "'";
        } else if (StrUtils.isEmpty(target) && type != null) {
            queryString = queryString
                    + " AND " + ChatConversationSQLiteHelper.COLUMN_TYPE + " = '" + type.getName() + "'";
        }
        queryString = queryString + ";";
        cursor = conversationDB.rawQuery(queryString, null);
        LogUtils.verbose(TAG, "Get " + cursor.getCount() + " conversations by user "
                + userId + " and target " + target);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatConversation conversation = createConversation(cursor);
            ChatMessage lastMessage = getLastMessage(userId, conversation.getType(), conversation.getTarget());
            conversation.setLastMessage(lastMessage);
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
        // This message exists, but the new message has not something new to update.
        if (isMessageExist(message)) {
            return;
        }

        // This message not exists, check if it is need to create conversation.
        String target = message.getRecipient();
        if (message.getChatType() == ChatType.SingleChat) {
            if (StrUtils.isEqual(target, userId)) {
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
            LogUtils.verbose(TAG, "insert " + message);
        }
    }

    public ChatMessage getLastMessage(String userId, ChatType type, String target) {
        ChatMessage lastMessage = null;
        String query = generateQueryBy(userId, type, target);
        query = query
                + " ORDER BY " + ChatMessageSQLiteHelper.COLUMN_MESSAGE_ID + " DESC "
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

    /***
     *
     * @param userId
     * @param type
     * @param target
     * @param startMessageId
     * @param endMessageId
     * @param limit
     * @return
     */
    public List<ChatMessage> getMessages(String userId, ChatType type, String target, Long startMessageId, Long endMessageId, int limit) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        Cursor cursor;
        String query= generateQueryBy(userId, type, target);
        if (startMessageId != null) {
            query += " AND " + ChatMessageSQLiteHelper.COLUMN_MESSAGE_ID + " <= " + startMessageId;
        }
        if (endMessageId != null) {
            query = query + " AND " + ChatMessageSQLiteHelper.COLUMN_MESSAGE_ID + " >= " + endMessageId;
        }
        query = query
                + " ORDER BY " + ChatMessageSQLiteHelper.COLUMN_MESSAGE_ID + " DESC "
                + " LIMIT " + limit
                + ";";
        cursor = messageDB.rawQuery(query, null);
        LogUtils.verbose(TAG, "Get " + cursor.getCount() + " messages between "
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
        String whereClause = ChatConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + ChatConversationSQLiteHelper.COLUMN_TYPE + " = ?"
                + " AND " + ChatConversationSQLiteHelper.COLUMN_TARGET + " = ?";
        ContentValues values = new ContentValues();
        values.put(ChatConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, 0);
        int rowsAffected = conversationDB.update(ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION,
                values, whereClause, new String[]{ userId, type.getName(), target });
        LogUtils.verbose(TAG, rowsAffected + " rows affected when reset unread count between " + userId + " and " + target);
    }

    public void remove(String userId, ChatType type, String target) {
        String whereClause = ChatConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + ChatConversationSQLiteHelper.COLUMN_TYPE + " = ?"
                + " AND " + ChatConversationSQLiteHelper.COLUMN_TARGET + " = ?";
        int rowsAffected = conversationDB.delete(ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, whereClause,
                new String[]{ userId, type.getName(), target });
        LogUtils.verbose(TAG, "Remove conversation:[type: " + type.getName() + ", target: " + target + "]"
                + " and " + rowsAffected + " rows affected");

        removeMessages(userId, type, target);
    }

    public int updateMessageFilePath(boolean isOriginal, String url, String filePath) {
        String urlColumn;
        String filePathColumn;
        if (isOriginal) {
            urlColumn = ChatMessageSQLiteHelper.COLUMN_FILE_URL;
            filePathColumn = ChatMessageSQLiteHelper.COLUMN_FILE_ORIGINAL_PATH;
        } else {
            urlColumn = ChatMessageSQLiteHelper.COLUMN_FILE_THUMB_URL;
            filePathColumn = ChatMessageSQLiteHelper.COLUMN_FILE_THUMBNAIL_PATH;
        }
        String whereClause = urlColumn + " = ?";
        ContentValues values = new ContentValues();
        values.put(filePathColumn, filePath);
        return messageDB.update(TABLE_MESSAGE, values, whereClause, new String[]{ url });
    }

    public void deleteAllData() {
        open();
        conversationDB.delete(TABLE_CONVERSATION, null, null);
        messageDB.delete(TABLE_MESSAGE, null, null);
    }

    private void insert(ChatConversation conversation, String userId) {
        ContentValues values = new ContentValues();
        values.put(ChatConversationSQLiteHelper.COLUMN_USER_ID, userId);
        values.put(ChatConversationSQLiteHelper.COLUMN_TARGET, conversation.getTarget());
        values.put(ChatConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(ChatConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(ChatConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());

        long id = conversationDB.insert(ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, null, values);
        LogUtils.verbose(TAG, "insert " + conversation + " with return id " + id);
    }

    private void removeMessages(String userId, ChatType type, String target) {
        String whereClause = "";
        int rowsAffected = 0;
        if (type == ChatType.GroupChat) {
            whereClause = ChatMessageSQLiteHelper.COLUMN_TO + " = ?";
            rowsAffected = messageDB.delete(ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE, whereClause,
                    new String[]{ target});
        } else {
            whereClause = "(" + ChatMessageSQLiteHelper.COLUMN_FROM + " = ?" + " AND " + ChatMessageSQLiteHelper.COLUMN_TO + " = ?)"
                    + " OR (" +  ChatMessageSQLiteHelper.COLUMN_FROM + " = ?" + " AND " + ChatMessageSQLiteHelper.COLUMN_TO + " = ?)";
            rowsAffected = messageDB.delete(ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE, whereClause,
                    new String[]{ userId, target, target, userId });
        }
        LogUtils.info(TAG, "Removed " + rowsAffected + " messages between " + userId + " and " + target);
    }

    /***
     * Update each field of conversation except *lastMessage*
     *
     * @param conversation
     * @param userId
     * @return rows effected
     */
    private int update(ChatConversation conversation, String userId) {
        String whereClause = ChatConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + ChatConversationSQLiteHelper.COLUMN_TARGET + " = ?";

        ContentValues values = new ContentValues();
        values.put(ChatConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(ChatConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(ChatConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());

        int rowsAffected = conversationDB.update(TABLE_CONVERSATION, values, whereClause,
                new String[]{ userId, conversation.getTarget() });
        LogUtils.verbose(TAG, "Update " + conversation + " and " + rowsAffected + " rows affected");
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
        ChatMessage message = new ChatMessage();
        message.setEngine(mEngine);

        message.setMessageId(cursor.getLong(1));
        message.setFrom(cursor.getString(2));
        message.setRecipient(cursor.getString(3));
        message.setChatType(ChatType.getType(cursor.getString(4)));

        String contentType = cursor.getString(6);
        ChatMessageContent content = new ChatMessageContent();
        content.setType(TYPE.getType(contentType));
        if (contentType.equals(TYPE.TEXT.getName())) {
            content.setText(cursor.getString(5));
        } else if (contentType.equals(TYPE.LOCATION.getName())) {
            ChatMessageLocationEntity location = ChatMessageLocationEntity.deserialize(cursor.getString(16));
            content = new ChatMessageLocationContent(location.getLat(), location.getLng(), location.getPoi());
        } else if (contentType.equals(TYPE.EVENT.getName())) {
            ChatMessageEventEntity event = new ChatMessageEventEntity();
            event.setType(ChatMessageEventEntity.TYPE.getType(cursor.getString(17)));
            event.setTarget(cursor.getString(18));

            content.setEvent(event);
        } else {
            ChatMessageFileEntity file = new ChatMessageFileEntity();
            file.setFrame(cursor.getInt(13), cursor.getInt(14));
            file.setDuration(cursor.getDouble(15));
            file.setFilePath(cursor.getString(7));
            file.setUrl(cursor.getString(8));
            file.setThumbnailPath(cursor.getString(9));
            file.setThumbUrl(cursor.getString(10));
            file.setSize(cursor.getDouble(11));
            file.setMimeType(cursor.getString(12));

            content.setFile(file);
        }

        String extraString = cursor.getString(19);
        Gson gson = new Gson();
        content.setExtra(gson.fromJson(extraString, JsonObject.class));

        message.setContent(content);
        message.setCreatedAt(cursor.getString(20));

        return message;
    }

    private String generateUniqueMessageId(ChatMessage message) {
        // These three value can unique specified a message
        return message.getFrom() + "#" + message.getRecipient() + "#" + message.getMessageId();
    }

    private boolean isConversationExist(String userId, String target) {
        if (StrUtils.isEqual(userId, target)) {
            return true;
        }

        String queryString = "SELECT * FROM " + ChatConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                + " WHERE " + ChatConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'"
                + " AND " + ChatConversationSQLiteHelper.COLUMN_TARGET + " = '" + target + "'"
                + ";";
        Cursor cursor = conversationDB.rawQuery(queryString, null);
        return cursor.getCount() > 0;
    }

    private boolean isMessageExist(ChatMessage message) {
        String uniqueMessageId = generateUniqueMessageId(message);
        String queryString = "SELECT * FROM " + ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                + " WHERE " + ChatMessageSQLiteHelper.COLUMN_ID + " = '" + uniqueMessageId + "'"
                + ";";
        Cursor cursor = messageDB.rawQuery(queryString, null);
        return cursor.getCount() > 0;
    }

    private long insertMessage(ChatMessage message) {
        ContentValues values =  new ContentValues();
        values.put(ChatMessageSQLiteHelper.COLUMN_ID, generateUniqueMessageId(message));
        values.put(ChatMessageSQLiteHelper.COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(ChatMessageSQLiteHelper.COLUMN_FROM, message.getFrom());
        values.put(ChatMessageSQLiteHelper.COLUMN_TO, message.getRecipient());
        values.put(ChatMessageSQLiteHelper.COLUMN_CHAT_TYPE, message.getChatType().getName());
        values.put(ChatMessageSQLiteHelper.COLUMN_CONTENT_TYPE, message.getContent().getType().getName());

        ChatMessageContent content = message.getContent();
        if (message.getContent().getType() == TYPE.TEXT) {
            values.put(ChatMessageSQLiteHelper.COLUMN_CONTENT, content.getText());
        } else if (isMediaMessage(message)) {
            ChatMessageFileEntity file = content.getFile();
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_ORIGINAL_PATH, file.getFilePath());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_THUMBNAIL_PATH, file.getThumbnailPath());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_URL, file.getUrl());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_THUMB_URL, file.getThumbUrl());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_SIZE, file.getSize());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_MIMETYPE, file.getMimeType());

            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_WIDTH, file.getWidth());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_HEIGHT, file.getHeight());
            values.put(ChatMessageSQLiteHelper.COLUMN_FILE_DURATION, file.getDuration());

        } else if (message.getContent().getType() == TYPE.LOCATION) {
            ChatMessageLocationEntity location = content.getLocation();
            values.put(ChatMessageSQLiteHelper.COLUMN_LOCATION, location.toString());
        } else if (message.getContent().getType() == TYPE.EVENT) {
            ChatMessageEventEntity event = message.getContent().getEvent();
            values.put(ChatMessageSQLiteHelper.COLUMN_EVENT_TYPE, event.getType().getName());
            values.put(ChatMessageSQLiteHelper.COLUMN_EVENT_TARGET, event.getTarget());
        }

        JsonElement extra = message.getContent().getExtra();
        LogUtils.debug(TAG, extra + "");
        if (extra != null) {
            values.put(ChatMessageSQLiteHelper.COLUMN_EXTRA, extra.toString());
        }

        values.put(ChatMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        return messageDB.insert(ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE, null, values);
    }

    private boolean isMediaMessage(ChatMessage message) {
        TYPE type = message.getContent().getType();
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
            queryString = "SELECT * FROM " + ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                    + " WHERE " + ChatMessageSQLiteHelper.COLUMN_CHAT_TYPE + " = '" + type.getName() + "'"
                    + " AND " + ChatMessageSQLiteHelper.COLUMN_TO + " = '" + target + "'";
        } else {
            queryString = "SELECT * FROM " + ChatMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                    + " WHERE " + ChatMessageSQLiteHelper.COLUMN_CHAT_TYPE + " = '" + type.getName() + "'"
                    + " AND "
                    + "((" + ChatMessageSQLiteHelper.COLUMN_FROM + " = '" + userId + "' AND " + ChatMessageSQLiteHelper.COLUMN_TO + " = '" + target
                    + "') OR "
                    + "(" + ChatMessageSQLiteHelper.COLUMN_FROM + " = '" + target + "' AND " + ChatMessageSQLiteHelper.COLUMN_TO + " = '" + userId
                    + "'))";
        }
        return queryString;
    }
}
