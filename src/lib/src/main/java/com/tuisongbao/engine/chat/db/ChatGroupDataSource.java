package com.tuisongbao.engine.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupDataSource {
    private SQLiteDatabase groupDB;
    private SQLiteDatabase groupMemberDB;
    private ChatGroupSQLiteHelper groupSQLiteHelper;
    private ChatGroupMemberSQLiteHelper groupMemberSQLiteHelper;
    private ChatManager mChatManager;
    private TSBEngine mEngine;

    public ChatGroupDataSource(Context context, TSBEngine engine) {
        mEngine = engine;
        mChatManager = engine.chatManager;
        groupSQLiteHelper = new ChatGroupSQLiteHelper(context);
        groupMemberSQLiteHelper = new ChatGroupMemberSQLiteHelper(context);
    }

    public void open() {
        groupDB = groupSQLiteHelper.getWritableDatabase();
        groupMemberDB = groupMemberSQLiteHelper.getWritableDatabase();
    }

    public void close() {
        groupSQLiteHelper.close();
        groupMemberSQLiteHelper.close();
    }

    public String getLatestLastActiveAt(String userId) {
        String sql = "SELECT * FROM " + ChatGroupSQLiteHelper.TABLE_CHAT_GROUP
                + " ORDER BY datetime(" + ChatConversationSQLiteHelper.COLUMN_LAST_ACTIVE_AT + ") DESC LIMIT 1";
        Cursor cursor = groupDB.rawQuery(sql, null);
        if (cursor.isAfterLast()) {
            return null;
        }
        cursor.moveToFirst();
        return cursor.getString(7);
    }

    /***
     * Try to update items, if not rows effected then insert.
     */
    public void upsert(ChatGroup group, String userId) {
        String groupId = group.getGroupId();
        int rowsEffected = update(group);
        if (rowsEffected < 1) {
            insert(group, userId);
        }
        insertUserIfNotExist(groupId, userId);
    }

    public void upsert(List<ChatGroup> groups, String userId) {
        for (ChatGroup group : groups) {
            upsert(group, userId);
        }
    }

    public void insert(ChatGroup group, String userId) {
        ContentValues values = getContentValuesExceptGroupId(group);
        values.put(ChatGroupSQLiteHelper.COLUMN_GROUP_ID, group.getGroupId());

        groupDB.insert(ChatGroupSQLiteHelper.TABLE_CHAT_GROUP, null, values);
        LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, "insert " + group);

        insertUserIfNotExist(group.getGroupId(), userId);
    }

    public List<ChatGroup> getList(String userId, String groupId) {
        List<ChatGroup> groups = new ArrayList<ChatGroup>();
        Cursor cursor = null;
        String sql = "SELECT * FROM " + ChatGroupMemberSQLiteHelper.TABLE_CHAT_GROUP_USER
                + " WHERE " + ChatGroupMemberSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        cursor = groupMemberDB.rawQuery(sql, null);
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " groups of user "
                + userId + ", groupId:" + groupId);

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

        String idClause = ChatGroupSQLiteHelper.COLUMN_GROUP_ID + inClauseString;
        sql = "SELECT * FROM " + ChatGroupSQLiteHelper.TABLE_CHAT_GROUP
                + " WHERE " + idClause;
        sql += ";";

        cursor = groupDB.rawQuery(sql, null);
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " groups");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatGroup group = createGroup(cursor);
            groups.add(group);
            cursor.moveToNext();
        }
        cursor.close();

        return groups;

    }

    public List<ChatGroup> getListNotWork(String userId, String groupId) {
        String sql = "SELECT group.name FROM "
                + ChatGroupSQLiteHelper.TABLE_CHAT_GROUP + " group INNER JOIN " + ChatGroupMemberSQLiteHelper.TABLE_CHAT_GROUP_USER + " groupUser"
                + " ON group." + ChatGroupMemberSQLiteHelper.COLUMN_GROUP_ID + " = groupUser." + ChatGroupSQLiteHelper.COLUMN_GROUP_ID
                + " WHERE groupUser." + ChatGroupMemberSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";

        String idClause = ChatGroupSQLiteHelper.COLUMN_GROUP_ID + " = '" + groupId + "'";

        if (groupId != null) {
            sql += " AND " + idClause;
        }
        sql += ";";
        Cursor cursor = groupDB.rawQuery(sql, null);
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " groups with query "
                + "[userId:" + userId + ", groupId:" + groupId + "]");

        List<ChatGroup> groups = new ArrayList<ChatGroup>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatGroup group = createGroup(cursor);
            groups.add(group);
            cursor.moveToNext();
        }
        cursor.close();

        return groups;
    }

    public int update(ChatGroup group) {
        String whereClause = ChatGroupSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        ContentValues values = getContentValuesExceptGroupId(group);

        int rowsAffected = groupDB.update(ChatGroupSQLiteHelper.TABLE_CHAT_GROUP, values, whereClause, new String[]{ group.getGroupId() });
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Update " + group + " and " + rowsAffected + " rows affected");
        return rowsAffected;
    }

    public List<ChatUser> getUsers(String groupId) {
        List<ChatUser> users = new ArrayList<ChatUser>();
        String whereClause = "SELECT " + ChatGroupMemberSQLiteHelper.COLUMN_USER_ID
                + " WHERE " + ChatGroupMemberSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        Cursor cursor = groupMemberDB.rawQuery(whereClause, new String[]{ groupId });
        LogUtil.verbose(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + cursor.getCount() + " users from group " + groupId);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatUser user = createGroupUser(cursor);
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
        String whereClause = "SELECT * FROM " + ChatGroupMemberSQLiteHelper.TABLE_CHAT_GROUP_USER
                + " WHERE " + ChatGroupMemberSQLiteHelper.COLUMN_GROUP_ID + " = '" + groupId + "'"
                + " AND " + ChatGroupMemberSQLiteHelper.COLUMN_USER_ID + " = '" + userId + "'";
        Cursor cursor = groupMemberDB.rawQuery(whereClause, null);

        if (cursor.getCount() < 1) {
            ContentValues values = new ContentValues();
            values.put(ChatGroupMemberSQLiteHelper.COLUMN_GROUP_ID, groupId);
            values.put(ChatGroupMemberSQLiteHelper.COLUMN_USER_ID, userId);
            groupMemberDB.insert(ChatGroupMemberSQLiteHelper.TABLE_CHAT_GROUP_USER, null, values);
            LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, groupId + " has new member " + userId);
        }
    }

    public void removeUser(String groupId, String userId) {
        String whereClause = ChatGroupMemberSQLiteHelper.COLUMN_GROUP_ID + " = ? AND "
                + ChatGroupMemberSQLiteHelper.COLUMN_USER_ID + " = ?";
        int rowsAffected = groupMemberDB.delete(ChatGroupMemberSQLiteHelper.TABLE_CHAT_GROUP_USER, whereClause, new String[]{ groupId, userId });
        LogUtil.info(LogUtil.LOG_TAG_CHAT_CACHE, "Remove user " + userId + " from " + groupId + ", " + rowsAffected + " rows affected");

        // Remove conversation
        ChatConversationDataSource dataSource = new ChatConversationDataSource(TSBEngine.getContext(), mEngine);
        dataSource.open();
        dataSource.remove(userId, ChatType.GroupChat, groupId);
        dataSource.close();

        // If the group is empty, remove this group.
        List<ChatGroup> groups = getList(userId, groupId);
        if (groups.size() < 1) {
            return;
        }
        ChatGroup group = groups.get(0);
        if (group.getUserCount() < 1) {
            remove(groupId, null);
        }
    }

    public void remove(String groupId, String userId) {
        String whereClause = ChatGroupSQLiteHelper.COLUMN_GROUP_ID + " = ?";
        int rowsAffected = groupDB.delete(ChatGroupSQLiteHelper.TABLE_CHAT_GROUP, whereClause, new String[]{ groupId });
        LogUtil.info(LogUtil.LOG_TAG_CHAT_CACHE, "Remove group " + groupId + " and " + rowsAffected + " rows affected");

        if (!StrUtil.isEmpty(userId)) {
            removeUser(groupId, userId);
        }
    }

    public void deleteAllData() {
        open();
        groupDB.delete(ChatGroupSQLiteHelper.TABLE_CHAT_GROUP, null, null);
        groupMemberDB.delete(ChatGroupMemberSQLiteHelper.TABLE_CHAT_GROUP_USER, null, null);
    }

    private ChatGroup createGroup(Cursor cursor) {
        ChatGroup group = new ChatGroup(mEngine);
        group.setGroupId(cursor.getString(1));
        group.setOwner(cursor.getString(2));
        group.setIsPublic(cursor.getInt(3) == 1 ? true : false);
        group.setUserCanInvite(cursor.getInt(4) == 1 ? true : false);
        group.setUserCount(cursor.getInt(5));
        group.setUserCountLimit(cursor.getInt(6));

        return group;
    }

    private ChatUser createGroupUser(Cursor cursor) {
        ChatUser user = new ChatUser();
        user.setUserId(cursor.getString(2));
        user.setPresence(cursor.getString(3));

        return user;
    }

    private ContentValues getContentValuesExceptGroupId(ChatGroup group) {
        ContentValues values = new ContentValues();
        values.put(ChatGroupSQLiteHelper.COLUMN_OWNER, group.getOwner());
        values.put(ChatGroupSQLiteHelper.COLUMN_ISPUBLIC, group.isPublic());
        values.put(ChatGroupSQLiteHelper.COLUMN_USER_CAN_INVITE, group.userCanInvite());
        values.put(ChatGroupSQLiteHelper.COLUMN_USER_COUNT, group.getUserCount());
        values.put(ChatGroupSQLiteHelper.COLUMN_USER_COUNT_LIMIT, group.getUserCountLimit());
        values.put(ChatGroupSQLiteHelper.COLUMN_LAST_ACTIVE_AT, group.getLastActiveAt());

        return values;
    }
}
