package com.tuisongbao.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.engine.utils.LogUtils;

class ChatMessageSQLiteHelper extends ChatBaseSQLiteHelper {
    public static final String TABLE_CHAT_MESSAGE = "chatMessage";

    private static final String TAG = "TSB" + ChatMessageSQLiteHelper.class.getSimpleName();
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
    public static final String COLUMN_FILE_ORIGINAL_PATH = "originalPath";
    public static final String COLUMN_FILE_URL = "url";
    public static final String COLUMN_FILE_THUMBNAIL_PATH = "thumbnailPath";
    public static final String COLUMN_FILE_THUMB_URL = "thumbUrl";
    public static final String COLUMN_FILE_SIZE = "size";
    public static final String COLUMN_FILE_MIMETYPE = "mimeType";
    /**
     * image's width and height
     */
    public static final String COLUMN_FILE_WIDTH = "width";
    public static final String COLUMN_FILE_HEIGHT = "height";
    /**
     * video and voice' duration
     */
    public static final String COLUMN_FILE_DURATION = "duration";
    /**
     * location
     */
    public static final String COLUMN_LOCATION = "location";
    /**
     * event
     */
    public static final String COLUMN_EVENT_TYPE = "eventType";
    public static final String COLUMN_EVENT_TARGET = "eventTarget";
    /**
     * extra
     */
    public static final String COLUMN_EXTRA = "extra";
    public static final String COLUMN_CREATED_AT = "createdAt";

    public ChatMessageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table if not exists "
            + TABLE_CHAT_MESSAGE + "(" + COLUMN_ID
            + " text primary key, "
            + COLUMN_MESSAGE_ID + " integer not null, "
            + COLUMN_FROM + " text not null, "
            + COLUMN_TO + " text not null, "
            + COLUMN_CHAT_TYPE + " text not null, "
            + COLUMN_CONTENT + " text, "
            + COLUMN_CONTENT_TYPE + " text not null, "

            + COLUMN_FILE_ORIGINAL_PATH + " text, "
            + COLUMN_FILE_URL + " text, "
            + COLUMN_FILE_THUMBNAIL_PATH + " text, "
            + COLUMN_FILE_THUMB_URL + " text, "
            + COLUMN_FILE_SIZE + " double, "
            + COLUMN_FILE_MIMETYPE + " text, "
            + COLUMN_FILE_WIDTH + " integer, "
            + COLUMN_FILE_HEIGHT + " integer, "
            + COLUMN_FILE_DURATION + " double, "

            + COLUMN_LOCATION + " text,"

            + COLUMN_EVENT_TYPE + " text, "
            + COLUMN_EVENT_TARGET + " text, "
            + COLUMN_EXTRA + " text, "
            + COLUMN_CREATED_AT + " text not null"
            + ");";
        LogUtils.debug(TAG, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtils.warn(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGE);
        onCreate(database);
    }
}
