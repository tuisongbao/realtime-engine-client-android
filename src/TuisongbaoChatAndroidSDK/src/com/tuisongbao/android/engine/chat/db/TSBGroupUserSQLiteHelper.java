package com.tuisongbao.android.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tuisongbao.android.engine.log.LogUtil;

/***
 * Relationship of group and user
 *
 * @author root
 *
 */
public class TSBGroupUserSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_CHAT_GROUP_USER = "chatGroupUser";
    private static final String DATABASE_NAME = "chatMessage.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public static final String COLUMN_GROUP_ID = "groupId";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_PRESENCE = "presence";

    public TSBGroupUserSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table "
          + TABLE_CHAT_GROUP_USER + "(" + COLUMN_ID
          + " integer primary key autoincrement, "
          + COLUMN_GROUP_ID + " text not null, "
          + COLUMN_USER_ID + " text not null "
          + COLUMN_PRESENCE + " text not null "
          + ");";
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_DB, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(LogUtil.LOG_TAG_CHAT_DB, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_GROUP_USER);
        onCreate(database);
    }
}
