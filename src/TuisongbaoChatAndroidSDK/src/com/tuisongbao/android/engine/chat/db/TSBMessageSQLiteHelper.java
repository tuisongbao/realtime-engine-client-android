package com.tuisongbao.android.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tuisongbao.android.engine.log.LogUtil;

public class TSBMessageSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_CHAT_MESSAGE = "chatMessage";
    private static final String DATABASE_NAME = "chatMessage.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public static final String COLUMN_MESSAGE_ID = "messageId";
    public static final String COLUMN_FROM = "from";
    public static final String COLUMN_TO = "to";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATED_AT = "createdAt";

    public TSBMessageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table "
          + TABLE_CHAT_MESSAGE + "(" + COLUMN_ID
          + " integer primary key autoincrement, "
          + COLUMN_MESSAGE_ID + " text not null, "
          + COLUMN_FROM + " text not null, "
          + COLUMN_TO + " text not null, "
          + COLUMN_TYPE + " text not null, "
          + COLUMN_CONTENT + " text not null, "
          + COLUMN_CREATED_AT + " text not null, "
          + ");";
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_DB, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(LogUtil.LOG_TAG_CHAT_DB, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGE);
        onCreate(database);
    }
}
