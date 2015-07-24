package com.tuisongbao.android.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.android.engine.log.LogUtil;

public class TSBConversationSQLiteHelper extends BaseSQLiteHelper {
    public static final String TABLE_CHAT_CONVERSATION = "chatConversation";
    private static final String DATABASE_NAME = "chatConversation.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_TARGET = "target";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_UNREAD_MESSAGE_COUNT = "unreadMessageCount";
    public static final String COLUMN_LAST_ACTIVE_AT = "lastActiveAt";
    public static final String COLUMN_GROUP_NAME = "groupName";

    public TSBConversationSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table if not exists "
          + TABLE_CHAT_CONVERSATION + "(" + COLUMN_ID
          + " integer primary key autoincrement, "
          + COLUMN_USER_ID + " text not null, "
          + COLUMN_TARGET + " text not null, "
          + COLUMN_TYPE + " text not null, "
          + COLUMN_UNREAD_MESSAGE_COUNT + " integer, "
          + COLUMN_LAST_ACTIVE_AT + " text, "
          + COLUMN_GROUP_NAME + " text"
          + ");";
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(LogUtil.LOG_TAG_CHAT_CACHE, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_CONVERSATION);
        onCreate(database);
    }
}