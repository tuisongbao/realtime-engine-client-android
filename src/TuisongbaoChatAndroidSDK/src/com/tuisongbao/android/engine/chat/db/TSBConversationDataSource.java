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
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
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

    public String getLatestLastActiveAt() {
        String sql = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
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

    private void insert(TSBChatConversation conversation, String userId) {
        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_USER_ID, userId);
        values.put(TSBConversationSQLiteHelper.COLUMN_TARGET, conversation.getTarget());
        values.put(TSBConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());

        conversationDB.insert(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, null, values);
        LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, "insert " + conversation);
    }

    public List<TSBChatConversation> getList(String userId, ChatType type, String target) {
        String queryString = "";
        Cursor cursor = null;
        List<TSBChatConversation> conversations = new ArrayList<TSBChatConversation>();

        if(!StrUtil.isEmpty(target)) {
            queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                    + " WHERE " + TSBConversationSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'"
                    + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = '" + target + "'"
                    + ";";
        } else if (StrUtil.isEmpty(target) && type != null) {
            queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                    + " WHERE " + TSBConversationSQLiteHelper.COLUMN_TYPE + " = '" + type.getName() + "'"
                    + ";";
        } else if (StrUtil.isEmpty(target) && type == null) {
            queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                    + ";";
        }
        cursor = conversationDB.rawQuery(queryString, null);

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
    public void upsertMessage(String userId, TSBMessage message) {
        // TODO: transaction
        if (isMessageExist(message)) {
            return;
        }

        String target = null;
        if (message.getChatType() == ChatType.GroupChat) {
            target = message.getRecipient();
        } else {
            target = message.getFrom();
        }
        boolean needCreateConversation = isConversationExist(userId, target);
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
    public List<TSBMessage> getMessages(ChatType type, String target, long startMessageId, long endMessageId) {
        List<TSBMessage> messages = new ArrayList<TSBMessage>();
        Cursor cursor = null;
        String queryString = "";
        String currentUserId = TSBChatManager.getInstance().getChatUser().getUserId();
        if (type == ChatType.GroupChat) {
            queryString = "SELECT * FROM " + TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                    + " WHERE " + TSBMessageSQLiteHelper.COLUMN_TYPE + " = '" + type.getName() + "'"
                    + " AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = '" + target + "'"
                    + ";";
        } else {
            queryString = "SELECT * FROM " + TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                    + " WHERE " + TSBMessageSQLiteHelper.COLUMN_TYPE + " = '" + type.getName() + "'"
                    + " AND "
                    + "((" + TSBMessageSQLiteHelper.COLUMN_FROM + " = '" + currentUserId + "' AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = '" + target
                    + "') OR "
                    + "(" + TSBMessageSQLiteHelper.COLUMN_FROM + " = '" + target + "' AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = '" + currentUserId
                    + "'))"
                    + " AND " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " > " + startMessageId
                    + " AND " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID + " < " + endMessageId
                    + " ORDER BY " + TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID
                    + ";";
        }
        cursor = messageDB.rawQuery(queryString, null);

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
        conversationDB.update(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION,
                values, whereClause, new String[]{ userId, type.getName(), target });
    }

    public void remove(String userId, ChatType type, String target) {
        String whereClause = TSBConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TYPE + " = ?"
                + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = ?";
        conversationDB.delete(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, whereClause,
                new String[]{ userId, type.getName(), target });

        whereClause = TSBMessageSQLiteHelper.COLUMN_TYPE + " = ?"
                + " AND " + TSBMessageSQLiteHelper.COLUMN_FROM + " = ?"
                + " AND " + TSBMessageSQLiteHelper.COLUMN_TO + " = ?";
        messageDB.delete(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, whereClause,
                new String[]{ type.getName(), userId, target });
    }

    public boolean isMessageExist(TSBMessage message) {
        String uniqueMessageId = generateUniqueMessageId(message);
        String queryString = "SELECT * FROM " + TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                + " WHERE " + TSBMessageSQLiteHelper.COLUMN_ID + " = '" + uniqueMessageId + "'"
                + ";";
        Cursor cursor = messageDB.rawQuery(queryString, null);
        return cursor.getCount() > 0;
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

        return conversationDB.update(TABLE_CONVERSATION, values, whereClause,
                new String[]{ currentUserId, conversation.getTarget() });
    }

    private TSBChatConversation createConversation(Cursor cursor) {
        TSBChatConversation conversation = new TSBChatConversation();
        conversation.setTarget(cursor.getString(2));
        conversation.setType(ChatType.getType(cursor.getString(3)));
        conversation.setUnreadMessageCount(cursor.getInt(4));
        conversation.setLastActiveAt(cursor.getString(5));

        return conversation;
    }

    private TSBMessage createMessage(Cursor cursor) {
        TSBMessage message = new TSBMessage();
        // TODO: string to long ??
        message.setMessageId(cursor.getLong(1));
        message.setFrom(cursor.getString(2));
        message.setRecipient(cursor.getString(3));
        message.setChatType(ChatType.getType(cursor.getString(4)));
        // TODO: multiple type body, image, voice and video
        TSBMessageBody body = TSBMessageBody.createMessage(TYPE.TEXT);
        body.setText(cursor.getString(5));
        message.setBody(body);
        message.setCreatedAt(cursor.getString(6));

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
        return cursor.getCount() < 1;
    }

    private long insertMessage(TSBMessage message) {
        ContentValues values =  new ContentValues();
        values.put(TSBMessageSQLiteHelper.COLUMN_ID, generateUniqueMessageId(message));
        values.put(TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(TSBMessageSQLiteHelper.COLUMN_FROM, message.getFrom());
        values.put(TSBMessageSQLiteHelper.COLUMN_TO, message.getRecipient());
        values.put(TSBMessageSQLiteHelper.COLUMN_TYPE, message.getChatType().getName());
        values.put(TSBMessageSQLiteHelper.COLUMN_CONTENT, message.getBody().getText());
        values.put(TSBMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        return messageDB.insert(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, null, values);
    }
}
