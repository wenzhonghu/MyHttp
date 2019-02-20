package com.xiaoniu.finance.myhttp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected static MessageDigest messagedigest = null;

    static {
        init();
    }

    private static void init() {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(MD5Util.class.getName()
                    + "初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }

    /**
     * 生成字符串的md5校验值
     */
    public synchronized static String getMD5String(String s) {
        try {
            return getMD5String(s.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成字符串的16位的md5校验值
     */
    public synchronized static String getMD5String16Bit(String s) {
        String md5 = getMD5String(s);
        if (md5 != null && md5.length() > 24) {
            return md5.substring(8, 24);
        }
        return md5;
    }

    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param password 要校验的字符串
     * @param md5PwdStr 已知的md5校验码
     */
    public synchronized static boolean checkPassword(String password,
            String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }

    /**
     * 生成文件的md5校验值
     */
    public synchronized static String getFileMD5String(File file)
            throws IOException {
        if (messagedigest == null) {
            init();
        }
        if (messagedigest == null) {
            return "";
        }
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, numRead);
        }
        fis.close();
        return bufferToHex(messagedigest.digest());
    }

    public synchronized static byte[] getFileMD5Bytes(File file,
            byte[] fileBytes) throws IOException {
        if (messagedigest == null) {
            init();
        }
        if (messagedigest == null) {
            return new byte[]{};
        }
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        int readOffset = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, numRead);
            System.arraycopy(buffer, 0, fileBytes, readOffset, numRead);
            readOffset += numRead;
        }
        fis.close();
        return messagedigest.digest();
    }

    /**
     * JDK1.4中不支持以MappedByteBuffer类型为参数update方法，并且网上有讨论要慎用MappedByteBuffer，
     * 原因是当使用 FileChannel.map 方法时，MappedByteBuffer 已经在系统内占用了一个句柄， 而使用
     * FileChannel.close 方法是无法释放这个句柄的，且FileChannel有没有提供类似 unmap 的方法，
     * 因此会出现无法删除文件的情况。
     *
     * 不推荐使用
     */
    public synchronized static String getFileMD5String_old(File file)
            throws IOException {
        if (messagedigest == null) {
            init();
        }
        if (messagedigest == null) {
            return "";
        }
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
                file.length());
        messagedigest.update(byteBuffer);
        return bufferToHex(messagedigest.digest());
    }

    public synchronized static String getMD5String(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (messagedigest == null) {
            init();
        }
        if (messagedigest == null) {
            return "";
        }
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }


    private synchronized static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private synchronized static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private synchronized static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        // 取字节中高 4 位的数字转换, >>>
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        // 取字节中低 4 位的数字转换
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
}
