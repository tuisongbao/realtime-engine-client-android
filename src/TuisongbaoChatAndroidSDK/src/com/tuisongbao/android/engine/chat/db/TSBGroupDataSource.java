package com.tuisongbao.android.engine.chat.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.log.LogUtil;
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

    public String getLatestLastActiveAt(String userId) {
        String sql = "SELECT * FROM " + TSBGroupSQLiteHelper.TABLE_CHAT_GROUP
                + " ORDER BY datetime(" + TSBConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT + ") DESC LIMIT 1";
        Cursor cursor = groupDB.rawQuery(sql, null);
        if (cursor.isAfterLast()) {
            return null;
        }
        cursor.moveToFirst();
        return cursor.getString(9);
    }

    /***
     * Try to update items, if not rows effected then insert.
     *
     * @param conversation
     * @param userId
     */
    public void upsert(TSBChatGroup group, String userId) {
        String groupId = group.getGroupId();
        int rowsEffected = update(group);
        if (rowsEffected < 1) {
            insert(group);
        }
        insertUserIfNotExist(groupId, userId);
    }

    public void upsert(List<TSBChatGroup> groups, String userId) {
        for (TSBChatGroup group : groups) {
            upsert(group, userId);
        }
    }

    public void insert(TSBChatGroup group) {
        ContentValues values = getContentValuesExceptGroupId(group);
        values.put(TSBGroupSQLiteHelper.COLUMN_GROUP_ID, group.getGroupId());

        groupDB.insert(TSBGroupSQLiteHelper.TABLE_CHAT_GROUP, null, values);
        LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, "insert " + group);
    }

    public List<TSBChatGroup> getList(String userId, String groupId, String groupName) {
        List<TSBChatGroup> groups = new ArrayList<TSBChatGroup>();
        Cursor cursor = null;
        String sql = "SELECT * FROM " + TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER
                + " WHERE " + TSBGroupUserSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        cursor = groupUserDB.rawQuery(sql, null);
        if (cursor.getCount() < 1) {
            return groups;
        }
        cursor.moveToFirst();
        List<String> usersGroupIdList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            usersGroupIdList.add(cursor.getString(1));
            cursor.moveToNext();
        }

        // If the user does not in this group.
        if (groupId != null && !usersGroupIdList.contains(groupId)) {
            return groups;
        }

        String inClauseString = "";
        if (groupId != null && usersGroupIdList.contains(groupId)) {
            inClauseString = " = '" + groupId + "'";
        } else if (groupId == null) {
            // Join ids to ('title1', 'title2', 'title3') format
            inClauseString = " IN ('" + usersGroupIdList.get(0) + "'";
            for (int i = 1; i < usersGroupIdList.size(); i++) {
                inClauseString = inClauseString + ",'" + usersGroupIdList.get(i) + "'";
            }
            inClauseString += ")";
        }

        String idClause = TSBGroupSQLiteHelper.COLUMN_GROUP_ID + inClauseString;
        String nameClause = TSBGroupSQLiteHelper.COLUMN_NAME + " LIKE '" + "%" + groupName + "%" + "'";
        sql = "SELECT * FROM " + TSBGroupSQLiteHelper.TABLE_CHAT_GROUP
                + " WHERE " + idClause;
        if (groupName != null) {
            sql += " AND " + nameClause;
        }
        sql += ";";

        cursor = groupDB.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBChatGroup group = createGroup(cursor);
            groups.add(group);
            cursor.moveToNext();
        }
        cursor.close();

        return groups;

    }

    public List<TSBChatGroup> getListNotWork(String userId, String groupId, String groupName) {
        String sql = "SELECT group.name FROM "
                + TSBGroupSQLiteHelper.TABLE_CHAT_GROUP + " group INNER JOIN " + TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER + " groupUser"
                + " ON group." + TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID + " = groupUser." + TSBGroupSQLiteHelper.COLUMN_GROUP_ID
                + " WHERE groupUser." + TSBGroupUserSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";

        String idClause = TSBGroupSQLiteHelper.COLUMN_GROUP_ID + " = '" + groupId + "'";
        String nameClause = TSBGroupSQLiteHelper.COLUMN_NAME + " LIKE '" + "%" + groupName + "%" + "'";

        if (groupId != null && groupName != null) {
            sql += " AND " + idClause + " AND " + nameClause;
        } else if (groupId == null && groupName != null) {
            sql += " AND " + nameClause;
        } else if (groupId != null && groupName == null) {
            sql += " AND " + idClause;
        }
        sql += ";";
        Cursor cursor = groupDB.rawQuery(sql, null);

        List<TSBChatGroup> groups = new ArrayList<TSBChatGroup>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TSBChatGroup group = createGroup(cursor);
            groups.add(group);
            cursor.moveToNext();
        }
        cursor.close();

        return groups;
    }

    public int update(TSBChatGroup group) {
        String whereClause = TSBGroupSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        ContentValues values = getContentValuesExceptGroupId(group);

        return groupDB.update(TSBGroupSQLiteHelper.TABLE_CHAT_GROUP, values, whereClause, new String[]{ group.getGroupId() });
    }

    public void remove(String groupId, String userId) {
        String whereClause = TSBGroupSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        groupDB.delete(TSBGroupSQLiteHelper.TABLE_CHAT_GROUP, whereClause, new String[]{ groupId });

        if (!StrUtil.isEmpty(userId)) {
            removeUser(groupId, userId);
        }
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

    /**
     * Query user in this group first, if not exist, insert the user
     *
     * @param groupId
     * @param userId
     */
    public void insertUserIfNotExist(String groupId, String userId) {
        String whereClause = "SELECT * FROM " + TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER
                + " WHERE " + TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID + " = '" + groupId + "'"
                + " AND " + TSBGroupUserSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        Cursor cursor = groupUserDB.rawQuery(whereClause, null);

        if (cursor.getCount() < 1) {
            ContentValues values = new ContentValues();
            values.put(TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID, groupId);
            values.put(TSBGroupUserSQLiteHelper.COLUMN_USER_ID, userId);
            groupUserDB.insert(TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER, null, values);
            LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, groupId + " has new member " + userId);
        }
    }

    public void removeUser(String groupId, String userId) {
        String whereClause = TSBGroupUserSQLiteHelper.COLUMN_GROUP_ID + " = ? AND "
                + TSBGroupUserSQLiteHelper.COLUMN_USER_ID + " = ?";
        groupUserDB.delete(TSBGroupUserSQLiteHelper.TABLE_CHAT_GROUP_USER, whereClause, new String[]{ groupId, userId });

        // TODO: if the group is empty, remove this group.
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

    private ContentValues getContentValuesExceptGroupId(TSBChatGroup group) {
        ContentValues values = new ContentValues();
        values.put(TSBGroupSQLiteHelper.COLUMN_OWNER, group.getOwner());
        values.put(TSBGroupSQLiteHelper.COLUMN_NAME, group.getName());
        values.put(TSBGroupSQLiteHelper.COLUMN_DESCRIPTION, group.getDescription());
        values.put(TSBGroupSQLiteHelper.COLUMN_ISPUBLIC, group.isPublic());
        values.put(TSBGroupSQLiteHelper.COLUMN_USER_CAN_INVITE, group.userCanInvite());
        values.put(TSBGroupSQLiteHelper.COLUMN_USER_COUNT, group.getUserCount());
        values.put(TSBGroupSQLiteHelper.COLUMN_USER_COUNT_LIMIT, group.getUserCountLimit());
        values.put(TSBGroupSQLiteHelper.COLUMN_LAST_ACTIVE_AT, group.getLastActiveAt());

        return values;
    }
}
