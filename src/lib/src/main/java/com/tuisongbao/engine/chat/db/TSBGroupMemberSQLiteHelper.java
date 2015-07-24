package com.tuisongbao.engine.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.engine.log.LogUtil;

/***
 * Relationship of group and user
 *
 * @author root
 *
 */
public class TSBGroupMemberSQLiteHelper extends BaseSQLiteHelper {
    public static final String TABLE_CHAT_GROUP_USER = "chatGroupMember";
    private static final String DATABASE_NAME = "chatGroupMember.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public static final String COLUMN_GROUP_ID = "groupId";
    public static final String COLUMN_USER_ID = "userId";

    public TSBGroupMemberSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createDatabaseString = "create table if not exists "
          + TABLE_CHAT_GROUP_USER + "(" + COLUMN_ID
          + " integer primary key autoincrement, "
          + COLUMN_GROUP_ID + " text not null, "
          + COLUMN_USER_ID + " text not null"
          + ");";
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, createDatabaseString);
        database.execSQL(createDatabaseString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LogUtil.warn(LogUtil.LOG_TAG_CHAT_CACHE, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_GROUP_USER);
        onCreate(database);
    }
}