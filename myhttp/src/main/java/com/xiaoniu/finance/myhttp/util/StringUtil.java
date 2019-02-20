package com.xiaoniu.finance.myhttp.util;

/**
 * Created by zhonghu on 2019/1/19.
 */

public class StringUtil {

    /**
     * 空字符
     */
    public static final String EMPTY = "";
    /**
     * N/A
     */
    public static final String NOT_AVALIBLE = "N/A";

    /**
     * 文本是否为空
     *
     * @return boolean
     */
    public static boolean isTextEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(value.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 文本是否不为空
     *
     * @return boolean
     */
    public static boolean isNotTextEmpty(String str) {
        return !isTextEmpty(str);
    }

    public static boolean isValueEmpty(String value){
        if(isTextEmpty(value)){
            return true;
        }
        if("0".equals(value)){
            return true;
        }
        if("0.0".equals(value)){
            return true;
        }
        if("null".equalsIgnoreCase(value)){
            return true;
        }
        return false;
    }
}
