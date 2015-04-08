package com.tuisongbao.android.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tuisongbao.android.engine.log.LogUtil;

public class TSBGroupSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_CHAT_GROUP = "chatGroup";
    private static final String DATABASE_NAME = "chatGroup.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public static final String COLUMN_GROUP_ID = "groupId";
    public static final String COLUMN_OWNER = "owner";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ISPUBLIC = "isPublic";
    public static final String COLUMN_USER_CAN_INVITE = "userCanInvite";
    public static final String COLUMN_USER_COUNT = "userCount";
    public static final String COLUMN_USER_COUNT_LIMIT = "userCountLimit";
    public static final String COLUMN_LAST_ACTIVE_AT = "lastActiveAt";

    public TSBGroupSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table "
          + TABLE_CHAT_GROUP + "(" + COLUMN_ID
          + " integer primary key autoincrement, "
          + COLUMN_GROUP_ID + " text not null, "
          + COLUMN_OWNER + " text not null, "
          + COLUMN_NAME + " text not null, "
          + COLUMN_DESCRIPTION + " text, "
          + COLUMN_ISPUBLIC + " integer,"
          + COLUMN_USER_CAN_INVITE + " integer, "
          + COLUMN_USER_COUNT + " integer, "
          + COLUMN_USER_COUNT_LIMIT + " integer, "
          + COLUMN_LAST_ACTIVE_AT + " text not null"
          + ");";
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_DB, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(LogUtil.LOG_TAG_CHAT_DB, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_GROUP);
        onCreate(database);
    }
}
