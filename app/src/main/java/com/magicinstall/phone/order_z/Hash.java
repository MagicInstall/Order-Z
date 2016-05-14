package com.magicinstall.phone.order_z;

import android.support.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要算法
 * 以及各种类型转换算法
 * Created by wing on 16/4/29.
 */
public class Hash {
    public final static String getMD5(String s) {
        return getMD5(s.getBytes());
    }
    public final static String getMD5(byte[] data) {
        char hexDigits[] = { '0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            //获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            //使用指定的字节更新摘要
            mdInst.update(data);
            //获得密文
            byte[] md = mdInst.digest();
            //把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public final static byte[] getSHA256(byte[] data){
        MessageDigest instance;
        try {
            //获得SHA256摘要算法的 MessageDigest 对象
            instance = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        // 传入内容
        instance.update(data);

        // 取得摘要
        byte[] sha256 = instance.digest();


        return sha256;
    }

    public final static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexString2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (char2Byte(hexChars[pos]) << 4 | char2Byte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte char2Byte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * int 转byte[4]
     * @param val
     * @return 小端序
     */
    public static byte[] int2byte32(int val) {
        return new byte[]{
                (byte)((val >>  0 ) & 0xFF), // 注意个扑街安卓右移系会保留符号位的!
                (byte)((val >>  8 ) & 0xFF),
                (byte)((val >> 16 ) & 0xFF),
                (byte)((val >> 24 ) & 0xFF)
        };
    }

    /**
     * byte[4] 转 int
     * <p>哩个方法会连续(如果有)读4个字节.
     * <P>FxxK the Android!!!
     * @param val
     * @param offset 在val 中的偏移位置
     * @return
     */
    public static int byte32ToInt(@NonNull byte val[], int offset) {
        int result = 0;
        if (val.length > (offset + 3)) {
            result |= val[offset + 3] & 0xFF;
            result <<= 8;
        }
        if (val.length > (offset + 2)) {
            result |= val[offset + 2] & 0xFF;
            result <<= 8;
        }
        if (val.length > (offset + 1)) {
            result |= val[offset + 1] & 0xFF;
            result <<=  8;
        }
        if (val.length > (offset + 0)) {
            result |= val[offset + 0] & 0xFF;
        }
        return result;
    }
}
