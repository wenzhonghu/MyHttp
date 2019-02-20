package com.xiaoniu.finance.myhttp.core;

/**
 * 字符串工具集
 */
public class StrUtils {

    /**
     * 空字符串常量 <br>
     * <br>
     * <i>佛曰：四大皆空</i>
     */
    public static final String EMPTY = "";
    /**
     * "不可使用"字符串常量
     */
    public static final String NOT_AVALIBLE = "N/A";

    /**
     * 判断字符串是否为空内容/空指针
     *
     * @param str 字符串
     * @return 是空内容/空指针，返回true，否则返回false
     */
    public static boolean isTextEmpty(String str) {
        return (str == null) || (str.length() < 1);
    }
}
