package com.xiaoniu.finance.myhttp.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import com.xiaoniu.finance.myhttp.http.Consts.CacheConst;
import java.io.File;

/**
 * 数据库帮助类
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final int DB_VERSION = 1;
    private static final String DB = "database";
    private static final String DB_NAME_DEFAULT = "xn_lib_data.db";

    private static volatile DatabaseHelper sDbHelper;

    private static String sDbName = "xn_lib_data.db";

    private static SQLiteDatabase getSQLiteDatabase(Context context) {
        SQLiteDatabase database;
        try {
            database = sDbHelper.getReadableDatabase();
        } catch (Exception e) {
            database = resetSqLiteDatabase(context);
        }
        return database;
    }

    private static SQLiteDatabase resetSqLiteDatabase(Context context) {
        if (!DB_NAME_DEFAULT.equals(sDbName)) {
            sDbName = DB_NAME_DEFAULT;
            sDbHelper = new DatabaseHelper(context, sDbName, null, DB_VERSION);
            return sDbHelper.getReadableDatabase();
        }
        return null;
    }


    /**
     * 获取SQLiteDatabase
     */
    public static SQLiteDatabase getSQLiteDatabase(Context context, boolean isWriteable) {
        if (sDbHelper == null) {
            synchronized (DatabaseHelper.class) {
                if (sDbHelper == null) {
                    sDbName = getDBName(context);
                    sDbHelper = new DatabaseHelper(context, sDbName, null, DB_VERSION);
                }
            }
        }
        if (isWriteable) {
            SQLiteDatabase database = null;
            try {
                database = sDbHelper.getWritableDatabase();
            } catch (Exception e) {
                database = resetSqLiteDatabase(context);
            }
            return database;
        } else {
            return getSQLiteDatabase(context);
        }
    }

    /**
     * 获取数据库名
     */
    private static String getDBName(Context context) {

        if (context == null) {
            return DB_NAME_DEFAULT;
        }
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (context.getExternalFilesDir(DB) == null) {
                return DB_NAME_DEFAULT;
            }
            return context.getExternalFilesDir(DB).getAbsolutePath() + File.separator + DB_NAME_DEFAULT;
        }
        return DB_NAME_DEFAULT;
    }

    /**
     * 释放数据库资源
     */
    public static void releaseDb() {
        if (sDbHelper == null) {
            return;
        }
        sDbHelper.close();
    }


    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * 创建缓存表及其索引
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //"create table if not exists t_cache(_id integer primary key autoincrement,c_key char(16),c_value text,c_type char(6), c_date integer)"
        StringBuilder cacheTable = new StringBuilder().append("create table if not exists ")
                .append(CacheConst.TABLE_NAME).append("(")
                .append(CacheConst.COLUMN_ID).append(" integer primary key autoincrement,")
                .append(CacheConst.COLUMN_KEY).append(" char(16),")
                .append(CacheConst.COLUMN_VALUE).append(" text,")
                .append(CacheConst.COLUMN_TYPE).append(" char(6),")
                .append(CacheConst.COLUMN_DATE).append(" integer)");
        //"CREATE UNIQUE INDEX IF NOT EXISTS ia ON t_cache(c_key)"
        StringBuilder cacheIndex = new StringBuilder().append("CREATE UNIQUE INDEX IF NOT EXISTS ia ON ").append(CacheConst.TABLE_NAME)
                .append("(").append(CacheConst.COLUMN_KEY).append(")");
        db.execSQL(cacheTable.toString());
        db.execSQL(cacheIndex.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
