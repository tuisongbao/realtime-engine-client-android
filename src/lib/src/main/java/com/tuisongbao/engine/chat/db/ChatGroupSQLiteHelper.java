package com.tuisongbao.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.engine.log.LogUtil;

public class ChatGroupSQLiteHelper extends ChatBaseSQLiteHelper {
    public static final String TABLE_CHAT_GROUP = "chatGroup";

    private static final String TAG = ChatGroupSQLiteHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "chatGroup.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public static final String COLUMN_GROUP_ID = "groupId";
    public static final String COLUMN_OWNER = "owner";
    public static final String COLUMN_ISPUBLIC = "isPublic";
    public static final String COLUMN_USER_CAN_INVITE = "userCanInvite";
    public static final String COLUMN_USER_COUNT = "userCount";
    public static final String COLUMN_USER_COUNT_LIMIT = "userCountLimit";
    public static final String COLUMN_LAST_ACTIVE_AT = "lastActiveAt";

    public ChatGroupSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table if not exists "
          + TABLE_CHAT_GROUP + "(" + COLUMN_ID
          + " integer primary key autoincrement, "
          + COLUMN_GROUP_ID + " text not null, "
          + COLUMN_OWNER + " text not null, "
          + COLUMN_ISPUBLIC + " integer,"
          + COLUMN_USER_CAN_INVITE + " integer, "
          + COLUMN_USER_COUNT + " integer, "
          + COLUMN_USER_COUNT_LIMIT + " integer, "
          + COLUMN_LAST_ACTIVE_AT + " text"
          + ");";
        LogUtil.debug(TAG, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_GROUP);
        onCreate(database);
    }
}
