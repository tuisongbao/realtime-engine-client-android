package com.tuisongbao.android.engine.chat.db;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.os.Environment;

import com.tuisongbao.android.engine.log.LogUtil;

public abstract class BaseSQLiteHelper extends SQLiteOpenHelper {

    public BaseSQLiteHelper(Context context, String name,
            CursorFactory factory, int version) {
        super(context, Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "/tuisongbao/" + File.separator
                + name, new CursorFactory() {

                    @Override
                    public Cursor newCursor(SQLiteDatabase arg0, SQLiteCursorDriver arg1,
                            String arg2, SQLiteQuery arg3) {
                        LogUtil.verbose(LogUtil.LOG_TAG_SQLITE, arg3.toString());
                        return new SQLiteCursor(arg1, arg2, arg3);
                    }
                }, version);
    }
}
