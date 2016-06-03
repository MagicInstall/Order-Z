package com.magicinstall.phone.order_z;

public class base64 {
    /**
     * BASE64解密
     *
     * @param key the String to be decrypted
     * @return byte[] the data which is decrypted
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return android.util.Base64.decode(key, android.util.Base64.DEFAULT);
    }
    /**
     * BASE64加密
     *
     * @param key the String to be encrypted
     * @return String the data which is encrypted
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return android.util.Base64.encodeToString(key, android.util.Base64.DEFAULT);
    }
}
