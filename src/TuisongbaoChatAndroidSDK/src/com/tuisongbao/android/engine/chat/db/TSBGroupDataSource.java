package com.tuisongbao.android.engine.chat.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBGroupDataSource {
    private SQLiteDatabase groupDB;
    private SQLiteDatabase groupUserDB;
    private TSBGroupSQLiteHelper groupSQLiteHelper;
    private TSBGroupUserSQLiteHelper groupUserSQLiteHelper;

    public TSBGroupDataSource(Context context) {
        groupSQLiteHelper = new TSBGroupSQLiteHelper(context);
        groupUserSQLiteHelper = new TSBGroupUserSQLiteHelper(context);
    }

    public void open() {
        groupDB = groupSQLiteHelper.getWritableDatabase();
        groupUserDB = groupUserSQLiteHelper.getWritableDatabase();
    }

    public void close() {
        groupSQLiteHelper.close();
        groupUserSQLiteHelper.close();
    }

    public void insert(TSBChatGroup group) {
        ContentValues values = new ContentValues();
        values.put(TSBGroupSQLiteHelper.COLUMN_GROUP_ID, group.getGroupId());
        values.put(TSBGroupSQLiteHelper.COLUMN_OWNER, group.getOwner());
        values.put(TSBGroupSQLiteHelper.COLUMN_NAME, group.getName());
        values.put(TSBGroupSQLiteHelper.COLUMN_DESCRIPTION, group.getDescription());
        values.put(TSBGroupSQLiteHelper.COLUMN_ISPUBLIC, group.isPublic());
        values.put(TSBGroupSQLiteHelper.COLUMN_USER_CAN_INVITE, group.userCanInvite());
        values.put(TSBGroupSQLiteHelper.COLUMN_USER_COUNT, group.getUserCount());
        values.put(TSBGroupSQLiteHelper.COLUMN_USER_COUNT_LIMIT, group.getUserCountLimit());

        groupDB.insert(TSBGroupSQLiteHelper.TABLE_CHAT_GROUP, null, values);
    }

    public List<TSBChatGroup> getList(String groupId, String groupName) {
        String whereClause = "";
        Cursor cursor = null;
        List<TSBChatGroup> groups = new ArrayList<TSBChatGroup>();

        if(!StrUtil.isEmpty(groupId)) {
            whereClause = "SELECT * FROM " + TSBGroupSQLiteHelper.TABLE_CHAT_GROUP
                + " WHERE " + TSBGroupSQLiteHelper.COLUMN_GROUP_ID + " = ?"
                + " AND " + TSBGroupSQLiteHelper.COLUMN_NAME + " LIKE ?"
                + ";";
            cursor = groupDB.rawQuery(whereClause, new String[]{ groupId, "%" + groupName + "%" });
        } else {
            whereClause = "SELECT * FROM " + TSBGroupSQLiteHelper.TABLE_CHAT_GROUP
                    + " WHERE " + TSBGroupSQLiteHelper.COLUMN_NAME + " LIKE ?"
                    + ";";
            cursor = groupDB.rawQuery(whereClause, new String[]{ "%" + groupName + "%" });
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBChatGroup group = createGroup(cursor);
            groups.add(group);
            cursor.moveToNext();
        }
        cursor.close();

        return groups;
    }

    public void remove(String groupId) {
        String whereClause = TSBGroupSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        groupDB.delete(TSBGroupSQLiteHelper.TABLE_CHAT_GROUP, whereClause, new String[]{ groupId });
    }

    public List<TSBChatGroupUser> getUsers(String groupId) {
        List<TSBChatGroupUser> users = new ArrayList<TSBChatGroupUser>();
        String whereClause = "SELECT " + TSBGroupUserSQLiteHelper.COLUMN_USER_ID
                + " WHERE " + TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        Cursor cursor = groupUserDB.rawQuery(whereClause, new String[]{ groupId });

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBChatGroupUser user = createGroupUser(cursor);
            users.add(user);
            cursor.moveToNext();
        }
        cursor.close();

        return users;
    }

    public void addUser(String groupId, String userId) {
        ContentValues values = new ContentValues();
        values.put(TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID, groupId);
        values.put(TSBGroupUserSQLiteHelper.COLUMN_USER_ID, userId);

        groupUserDB.insert(TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER, null, values);
    }

    public void removeUser(String groupId, String userId) {
        String whereClause = TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID + " = ? AND "
                + TSBGroupUserSQLiteHelper.COLUMN_USER_ID + " = ?";
        groupUserDB.delete(TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER, whereClause, new String[]{ groupId, userId });

        // 如果这个group已经没有人了，删除这个group
        List<TSBChatGroup> groups = getList(groupId, null);
        if (groups.get(0).getUserCount() < 1) {
            remove(groupId);
        }
    }

    private TSBChatGroup createGroup(Cursor cursor) {
        TSBChatGroup group = new TSBChatGroup();
        group.setGroupId(cursor.getString(1));
        group.setOwner(cursor.getString(2));
        group.setName(cursor.getString(3));
        group.setDescription(cursor.getString(4));
        group.setIsPublic(cursor.getInt(5) == 1 ? true : false);
        group.setUserCanInvite(cursor.getInt(6) == 1 ? true : false);
        group.setUserCount(cursor.getInt(7));
        group.setUserCountLimit(cursor.getInt(8));

        return group;
    }

    private TSBChatGroupUser createGroupUser(Cursor cursor) {
        TSBChatGroupUser user = new TSBChatGroupUser();
        user.setUserId(cursor.getString(2));
        user.setPresence(cursor.getString(3));

        return user;
    }
}
