package com.xiaoniu.finance.myhttp.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.http.Consts.CacheConst;
import com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.finance.myhttp.http.request.Response.Builder;
import com.xiaoniu.finance.myhttp.util.AES;
import com.xiaoniu.finance.myhttp.util.DatabaseHelper;
import com.xiaoniu.finance.myhttp.util.MD5Util;
import com.xiaoniu.finance.myhttp.util.StringUtil;

/**
 * 数据库缓存实现<br>
 * 默认提供数据加密
 */
public class DatabaseCacheController implements ICacheController {

    private static final String TAG = "DatabaseCacheController";

    private boolean isSupportCryption = true;

    public DatabaseCacheController() {
    }


    @Override
    public boolean isCache(Request request) {
        if (request == null || StringUtil.isTextEmpty(request.getUrl())) {
            return false;
        }
        String entireUrl = request.getRequestEntireUrl();
        String columnKey = MD5Util.getMD5String16Bit(entireUrl);
        Cursor cursor = null;
        try {
            cursor = DatabaseHelper.getSQLiteDatabase(Global.getContext(), false)
                    .query(CacheConst.TABLE_NAME, new String[]{CacheConst.COLUMN_VALUE},
                            CacheConst.COLUMN_KEY + "='" + columnKey + "'",
                            null, null, null, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DatabaseHelper.releaseDb();
        }

    }

    @Override
    public void getResponseFromCache(Request request) {
        try {
            responseCache(Global.getContext(), request, request.getOnRequestListener());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 如果获取数据成功，则将请求数据保存在cache中
     */
    @Override
    public void saveResponseToCache(Object result, Request request, Response response) {

        if (result == null || request == null) {
            Log.d(TAG, "saveDateAsCache result is null");
            return;
        }
        String cacheType = String.valueOf(request.isCacheData().getType());
        String columnKey = MD5Util.getMD5String16Bit(request.getRequestEntireUrl());
        String resultStr = encode(response.getHttpRetData());
        ContentValues values = new ContentValues(3);
        values.put(CacheConst.COLUMN_KEY, columnKey);
        values.put(CacheConst.COLUMN_VALUE, resultStr);
        values.put(CacheConst.COLUMN_TYPE, cacheType);
        values.put(CacheConst.COLUMN_DATE, System.currentTimeMillis());
        SQLiteDatabase db = DatabaseHelper.getSQLiteDatabase(Global.getContext(), false);
        int rowNum = db.update(CacheConst.TABLE_NAME, values, CacheConst.COLUMN_KEY + "='" + columnKey + "'", null);
        Log.d(TAG, "saveDateAsCache rowNum:" + rowNum);
        if (rowNum == 0) {
            long id = db.insert(CacheConst.TABLE_NAME, null, values);
            Log.d(TAG, "saveDateAsCache id:" + id);
        }
        DatabaseHelper.releaseDb();
    }

    private String encode(String str) {
        if (isSupportCryption) {
            try {
                return Base64.encodeToString(AES.encode(str), Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                return str;
            }
        }
        return str;
    }

    private String decode(String str) {
        if (isSupportCryption) {
            try {
                return AES.decode(Base64.decode(str, Base64.DEFAULT));
            } catch (Exception e) {
                e.printStackTrace();
                return str;
            }
        }
        return str;
    }


    /**
     * 从数据库中读取缓存
     */
    private void responseCache(Context context, Request request, OnRequestListener originalListener) {

        if (originalListener == null) {
            return;
        }
        String entireUrl = request.getRequestEntireUrl();
        String columnKey = MD5Util.getMD5String16Bit(entireUrl);
        Cursor cursor = DatabaseHelper.getSQLiteDatabase(context, false)
                .query(CacheConst.TABLE_NAME, new String[]{CacheConst.COLUMN_VALUE}, CacheConst.COLUMN_KEY + "='" + columnKey + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            String jsonData = decode(cursor.getString(0));
            Log.d(TAG, "responseCache url:" + entireUrl + ",jsonData:" + jsonData);
            if (!TextUtils.isEmpty(jsonData)) {
                Builder builder = new Builder().code(HttpRespCode.STATE_SUC).data(jsonData);
                try {
                    Object result = request.getParser().parseData(jsonData);
                    originalListener.onResponse(request.getUrl(), HttpRespCode.STATE_SUC, result, request.getRequestType(), request, builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        cursor.close();
        DatabaseHelper.releaseDb();
    }


    private void deleteCache(SQLiteDatabase db, Request request) {
        String entireUrl = request.getRequestEntireUrl();
        String columnKey = MD5Util.getMD5String16Bit(entireUrl);
        Cursor cursor = DatabaseHelper.getSQLiteDatabase(Global.getContext(), false)
                .query(CacheConst.TABLE_NAME, new String[]{CacheConst.COLUMN_ID}, CacheConst.COLUMN_KEY + "='" + columnKey + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            db.delete(CacheConst.TABLE_NAME, CacheConst.COLUMN_ID + "=" + id, null);
        }
        cursor.close();
    }

    /**
     * 清空当前请求的缓存
     */
    @Override
    public void clearCache(Request request) {
        Log.d(TAG, "clearOldCache 删除旧的缓存");
        try {
            SQLiteDatabase db = DatabaseHelper.getSQLiteDatabase(Global.getContext(), false);
            deleteCache(db, request);
            DatabaseHelper.releaseDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除全部缓存
     */
    @Override
    public void clearAllCache() {
        DatabaseHelper.getSQLiteDatabase(Global.getContext(), false).delete(CacheConst.TABLE_NAME, null, null);
        DatabaseHelper.releaseDb();
    }


    /**
     * 是否需要提供加解密保证数据缓存的安全
     *
     * @param supportCryption true表示支持加解密，false表示不需加解密
     */
    public DatabaseCacheController setSupportCryption(boolean supportCryption) {
        isSupportCryption = supportCryption;
        return this;
    }
}
