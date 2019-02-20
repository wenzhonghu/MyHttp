package com.xiaoniu.myhttpdemo.cookie;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.finance.myhttp.http.cookie.CookieController;
import com.xiaoniu.finance.myhttp.http.cookie.SaveCookies;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by zhonghu on 2019/2/19.
 */

public class AppCookieController implements CookieController {

    private static final String TAG = "AppCookieController";
    private static final String COOKIE_PATH = "xxxx";

    @Override
    public void saveCookies(Context context, List<SaveCookies> cookies) {
        String jsonStr = new Gson().toJson(cookies);
        boolean saveResult = saveData(context, jsonStr, COOKIE_PATH);
        Log.v(TAG, "saveCookieFile " + saveResult);
    }

    @Override
    public List<SaveCookies> loadCookies(Context context) {
        String readStr = readFileAsString(context, COOKIE_PATH);
        if (readStr == null) {
            Log.e(TAG, "readCookieFile readFileAsString failed");
            return null;
        }
        List<SaveCookies> list = null;
        try {
            list = new Gson().fromJson(readStr, new TypeToken<List<SaveCookies>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * [保存数据到文件]
     */
    private static boolean saveData(Context context, String data, String filePath) {
        if (filePath == null || "".equals(filePath)) {
            return false;
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(filePath, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write(data);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }


    /**
     * [读取文本文件] 将文本内容的每一行读取到List的Item中
     */
    private static String readFileAsString(Context context, String dataFilePath) {

        if (dataFilePath == null || "".equals(dataFilePath)) {
            return null;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = context.openFileInput(dataFilePath);
            byte readBytes[] = new byte[inputStream.available()];
            inputStream.read(readBytes);
            String result = new String(readBytes, "utf-8");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
