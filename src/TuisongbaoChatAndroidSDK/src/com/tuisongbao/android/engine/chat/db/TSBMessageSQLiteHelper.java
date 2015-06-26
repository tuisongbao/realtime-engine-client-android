package com.tuisongbao.android.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.android.engine.log.LogUtil;

public class TSBMessageSQLiteHelper extends BaseSQLiteHelper {
    public static final String TABLE_CHAT_MESSAGE = "chatMessage";
    private static final String DATABASE_NAME = "chatMessage.db";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MESSAGE_ID = "messageId";
    // key word "from" is not allowed, it will cause syntax error
    public static final String COLUMN_FROM = "sender";
    public static final String COLUMN_TO = "receiver";
    public static final String COLUMN_CHAT_TYPE = "chatType";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CONTENT_TYPE = "contentType";
    /**
     * file properties
     */
    public static final String COLUMN_FILE_LOCAL_PATH = "localPath";
    public static final String COLUMN_FILE_DOWNLOAD_URL = "downloadUrl";
    public static final String COLUMN_FILE_SIZE = "size";
    public static final String COLUMN_FILE_MIMETYPE = "mimeType";
    /**
     * image's width & height
     */
    public static final String COLUMN_FILE_WIDTH = "width";
    public static final String COLUMN_FILE_HEIGHT = "height";
    /**
     * video's duration
     */
    public static final String COLUMN_FILE_DURATION = "duration";
    /**
     * event
     */
    public static final String COLUMN_EVENT_TYPE = "eventType";
    public static final String COLUMN_EVENT_TARGET = "eventTarget";

    public static final String COLUMN_CREATED_AT = "createdAt";

    public TSBMessageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table if not exists "
          + TABLE_CHAT_MESSAGE + "(" + COLUMN_ID
          + " text primary key, "
          + COLUMN_MESSAGE_ID + " integersq not null, "
          + COLUMN_FROM + " text not null, "
          + COLUMN_TO + " text not null, "
          + COLUMN_CHAT_TYPE + " text not null, "
          + COLUMN_CONTENT + " text, "
          + COLUMN_CONTENT_TYPE + " text not null, "

          + COLUMN_FILE_LOCAL_PATH + " text, "
          + COLUMN_FILE_DOWNLOAD_URL + " text, "
          + COLUMN_FILE_SIZE + " text, "
          + COLUMN_FILE_MIMETYPE + " text, "
          + COLUMN_FILE_WIDTH + " integer, "
          + COLUMN_FILE_HEIGHT + " integer, "
          + COLUMN_FILE_DURATION + " text, "

          + COLUMN_EVENT_TYPE + " text, "
          + COLUMN_EVENT_TARGET + " text, "
          + COLUMN_CREATED_AT + " text not null"
          + ");";
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(LogUtil.LOG_TAG_CHAT_CACHE, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGE);
        onCreate(database);
    }
}
