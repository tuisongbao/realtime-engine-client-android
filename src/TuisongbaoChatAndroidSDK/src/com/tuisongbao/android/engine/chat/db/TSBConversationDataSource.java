package com.tuisongbao.android.engine.chat.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBConversationDataSource {
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

    public void insert(TSBChatConversation conversation, String userId) {
        ContentValues values = new ContentValues();
        values.put(TSBConversationSQLiteHelper.COLUMN_USER_ID, userId);
        values.put(TSBConversationSQLiteHelper.COLUMN_TARGET, conversation.getTarget());
        values.put(TSBConversationSQLiteHelper.COLUMN_TYPE, conversation.getType().getName());
        values.put(TSBConversationSQLiteHelper.COLUMN_UNREAD_MESSAGE_COUNT, conversation.getUnreadMessageCount());
        values.put(TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT, conversation.getLastActiveAt());

        conversationDB.insert(TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION, null, values);
    }

    public List<TSBChatConversation> getList(String userId, ChatType type, String target) {
        String queryString = "";
        Cursor cursor = null;
        List<TSBChatConversation> conversations = new ArrayList<TSBChatConversation>();

        if(!StrUtil.isEmpty(target)) {
            queryString = "SELECT * FROM " + TSBConversationSQLiteHelper.TABLE_CHAT_CONVERSATION
                    + " WHERE " + TSBConversationSQLiteHelper.COLUMN_USER_ID + " = ?"
                    + " AND " + TSBConversationSQLiteHelper.COLUMN_TARGET + " = ?"
                    + ";";
            cursor = conversationDB.rawQuery(queryString, new String[]{ userId, target });
        } else {
            queryString = "SELECT * FROM " + TSBGroupSQLiteHelper.TABLE_CHAT_GROUP
                    + " WHERE " + TSBConversationSQLiteHelper.COLUMN_TYPE + " = ?"
                    + ";";
            cursor = conversationDB.rawQuery(queryString, new String[]{ type.getName() });
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBChatConversation conversation = createConversation(cursor);
            conversations.add(conversation);
            cursor.moveToNext();
        }
        cursor.close();

        return conversations;
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

    /**
     * When new message received or first message of chat, there is no such conversation of message's from and to,
     * so it will create a conversation for it.
     *
     * @param message
     */
    public void addMessage(String userId, TSBMessage message) {
        List<TSBChatConversation> conversations = getList(userId, message.getChatType(),
                message.getRecipient());
        // TODO: is there a way to realize upsert?
        if (conversations.size() < 1) {
            TSBChatConversation conversation = new TSBChatConversation();
            conversation.setTarget(message.getRecipient());
            conversation.setType(message.getChatType());
            conversation.setUnreadMessageCount(1);
            insert(conversation, userId);
        }
        // TODO: need transmission??
        ContentValues values =  new ContentValues();
        values.put(TSBMessageSQLiteHelper.COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(TSBMessageSQLiteHelper.COLUMN_FROM, message.getFrom());
        values.put(TSBMessageSQLiteHelper.COLUMN_TO, message.getRecipient());
        values.put(TSBMessageSQLiteHelper.COLUMN_TYPE, message.getChatType().getName());
        values.put(TSBMessageSQLiteHelper.COLUMN_CONTENT, message.getBody().getText());
        values.put(TSBMessageSQLiteHelper.COLUMN_CREATED_AT, message.getCreatedAt());

        messageDB.insert(TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE, null, values);
    }


    /**
     * Conversation's type and target map the message's type and to field relatively.
     *
     * @param type
     * @param target
     * @return List<TSBMessage>
     */
    public List<TSBMessage> getMessages(ChatType type, String from, String to) {
        List<TSBMessage> messages = new ArrayList<TSBMessage>();
        Cursor cursor = null;
        String queryString = "SELECT * FROM " + TSBMessageSQLiteHelper.TABLE_CHAT_MESSAGE
                + " WHERE " + TSBMessageSQLiteHelper.COLUMN_TYPE + " is ? "
                + " AND " + TSBMessageSQLiteHelper.COLUMN_FROM + " is ?"
                + " AND " + TSBMessageSQLiteHelper.COLUMN_TO + " is ?"
                + ";";
        cursor = messageDB.rawQuery(queryString, new String[] { type.getName(), from, to });

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBMessage message = createMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();

        return messages;
    }


    private TSBChatConversation createConversation(Cursor cursor) {
        TSBChatConversation conversation = new TSBChatConversation();
        conversation.setTarget(cursor.getString(2));
        conversation.setType(ChatType.getType(cursor.getString(3)));
        // TODO: keep messageId ? what's the use of it ?
        conversation.setUnreadMessageCount(cursor.getInt(5));

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
}
