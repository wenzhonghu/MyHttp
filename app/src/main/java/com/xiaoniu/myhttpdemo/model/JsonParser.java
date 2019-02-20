package com.xiaoniu.myhttpdemo.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xiaoniu.finance.myhttp.http.parser.IDataParser;

/**
 * Gson 的注解
 *
 * 字段别名:
 *
 * @SerializedName("name") int a;
 *
 * 忽略字段:
 * @Expose int b;
 */
public class JsonParser implements IDataParser {

    private static Gson sGson;

    static {
        sGson = new GsonBuilder()
                // .excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
                // .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")//时间转化为特定格式
                // .setPrettyPrinting() //对json结果格式化.
                // .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
                // .serializeNulls()
                .create();
    }

    java.lang.reflect.Type parseType;

    public JsonParser(java.lang.reflect.Type parseType) {
        this.parseType = parseType;
    }

    @Override
    public Object parseData(String data) {
        return sGson.fromJson(data, parseType);
    }

}
