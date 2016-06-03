package com.magicinstall.library;

import android.content.Context;
import android.support.annotation.RawRes;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class RSA {
    private static final String TAG = "RSA";

    private static final int KEY_BIT    = 1024;             // 密匙长度
    private static final int POCKET_LEN = KEY_BIT / 8 - 11; // PKCS1 要求限制明文长度

    private Key mKey = null;
    private Cipher mCipher = null;

    /**
     * @return 返回私匙是否已载入
     */
    public boolean hasPrivateKeyLoaded() {
        if (mKey == null) return false;
        return mKey.getClass().equals(RSAPrivateKey.class);
    }

    /**
     * @return 返回公匙是否已载入
     */
    public boolean hasPublicKeyLoaded() {
        if (mKey == null) return false;
        return mKey.getClass().equals(RSAPublicKey.class);
    }

    /**
     * 取得每个加密块的长度
     * @return
     */
    public int getEncryptBlockSize() {
        if (mCipher == null) return -1;
        try {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "哩个密匙无法初始化算法器"); return -1;
        }
        return mCipher.getBlockSize();
    }

    // TODO: 取得公匙 byte[]

//    public static final String KEY_ALGORITHM = "RSA";
//    public static final String KEY_ALGORITHM = "RSA/ECB/PKCS1Padding";
//    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
//
//    private static final String PUBLIC_KEY = "RSAPublicKey";
//    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /************************************************************************
     *                              计算过程                                 *
     ************************************************************************/

    private void initCipher(){

    }

    /**
     * 解密过程
     * <p>注意:哩个方法要传入正确的偏移地址同埋长度.
     * @param data
     * @param offset
     * @param length
     * @return
     */
    public byte[] decrypt(byte[] data, int offset, int length) {
        if (mKey == null){
            Log.e(TAG, "未载入密匙"); return null;
        }

        try {
//            mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// TODO: 删除哩句
            mCipher.init(Cipher.DECRYPT_MODE, mKey);
//        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, "无此算法"); return null;
//        } catch (NoSuchPaddingException e) {
//            Log.e(TAG, "无此填充方式"); return null;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "密匙非法"); return null;
        }

        // 拆包
        int block_size = mCipher.getBlockSize();
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream(block_size);
        int block_idx = 0;
        try {
            while (length - block_idx * block_size > 0) {
                output_stream.write(mCipher.doFinal(data, block_idx * block_size + offset, block_size));
                block_idx++;
            }
        } catch (IOException e) {
            Log.e(TAG, "字节流写入错误"); return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "解码输出的长度不是块的长度???"); return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "填充方式与数据不匹配"); return null;
        }

        return output_stream.toByteArray();
    }

        /**
         * 解密过程
         * @param data 密文数据
         * @return 明文
         */
    public byte[] decryptData(byte[] data) {
        if (mKey == null){
            Log.e(TAG, "未载入密匙"); return null;
        }

        try {
            mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// TODO: 删除哩句
            mCipher.init(Cipher.DECRYPT_MODE, mKey);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "无此算法"); return null;
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "无此填充方式"); return null;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "密匙非法"); return null;
        }

        // 拆包
        int block_size = mCipher.getBlockSize();
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream(128);
        int block_idx = 0;
        try {
            while (data.length - block_idx * block_size > 0) {
                output_stream.write(mCipher.doFinal(data, block_idx * block_size, block_size));
                block_idx++;
            }
        } catch (IOException e) {
            Log.e(TAG, "字节流写入错误"); return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "解码输出的长度不是块的长度???"); return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "填充方式与数据不匹配"); return null;
        }

        return output_stream.toByteArray();
    }


    /**
     * 加密过程
     * @param data 明文数据
     * @return
     */
    public byte[] encrypt(byte[] data, int offset, int length) {
        if (mKey == null) {Log.e(TAG, "未载入密匙");return null;}

        try {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "哩个密匙无法初始化算法器"); return null;
        }
        // 拆包
        // 反正就系PKCS1 要求每次加密的长度唔过以超过密匙嘅模数
        int block_size  = mCipher.getBlockSize(); // 取得每次运算的最大长度
        int output_size = mCipher.getOutputSize(length); // 获得加密后的长度
        int leaved_size = length % block_size; // 最后一次传入的数据的长度
        int blocks_cnt  = leaved_size != 0 ? length / block_size + 1 : length / block_size;
        byte[] output_buffer = new byte[output_size * blocks_cnt];
        int block_idx = 0;
        try {
            while (length - block_idx * block_size > 0) {
                if (length - block_idx * block_size > block_size)
                    mCipher.doFinal(data, offset + block_idx * block_size, block_size, output_buffer, block_idx * output_size);

                else
                    mCipher.doFinal(data, offset + block_idx * block_size, length - block_idx * block_size, output_buffer, block_idx * output_size);

                block_idx++;
                Log.v(TAG, "Encrypt block " + block_idx);
            }
        } catch (ShortBufferException e) {
            Log.e(TAG, "output_buffer 唔够大"); return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "解码输出的长度不是块的长度???"); return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "填充方式与数据不匹配"); return null;
        }
        return output_buffer;
    }

    /** TODO: 合并为一个方法
     * 加密过程
     * @param data 明文数据
     * @return
     */
    public byte[] encrypt(byte[] data) {
        if (mKey == null) {Log.e(TAG, "未载入密匙");return null;}

        try {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "哩个密匙无法初始化算法器"); return null;
        }

        // 拆包
        // 反正就系PKCS1 要求每次加密的长度唔过以超过密匙嘅模数
        int block_size  = mCipher.getBlockSize(); // 取得每次运算的最大长度
        int output_size = mCipher.getOutputSize(data.length); // 获得加密后的长度
        int leaved_size = data.length % block_size; // 最后一次传入的数据的长度
        int blocks_cnt  = leaved_size != 0 ? data.length / block_size + 1 : data.length / block_size;
        byte[] output_buffer = new byte[output_size * blocks_cnt];
        int block_idx = 0;
        try {
            while (data.length - block_idx * block_size > 0) {
                if (data.length - block_idx * block_size > block_size)
                    mCipher.doFinal(data, block_idx * block_size, block_size, output_buffer, block_idx * output_size);

                else
                    mCipher.doFinal(data, block_idx * block_size, data.length - block_idx * block_size, output_buffer, block_idx * output_size);

                block_idx++;
                Log.v(TAG, "Encrypt block " + block_idx);
            }
        } catch (ShortBufferException e) {
            Log.e(TAG, "output_buffer 唔够大"); return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "解码输出的长度不是块的长度???"); return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "填充方式与数据不匹配"); return null;
        }
        return output_buffer;
    }

    /************************************************************************
     *                              加载密匙                                 *
     ************************************************************************/

    /**
     * 从字符串中加载公钥
     * @param publicKeyStr 公钥数据字符串
     * @throws InvalidKeySpecException 公钥非法
     */
    public RSAPublicKey loadPublicKey(String publicKeyStr) throws InvalidKeySpecException {
        byte[] buffer = Base64.decode(publicKeyStr, Base64.DEFAULT);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);

        KeyFactory keyFactory = null;
//        Key public_key = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "无此算法");
        }

        mKey = keyFactory.generatePublic(keySpec); // 哩句会抛出异常

        // 喺哩度初始化算法器
        if (mCipher == null) {
            try {
                mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "无此算法");
                return null;
            } catch (NoSuchPaddingException e) {
                Log.e(TAG, "无此填充方式");
                return null;
            }
        }
        return (RSAPublicKey) mKey;
    }

    /**
     * 从文件输入流中加载公钥
     * @param keyIn 公钥输入流
     * @throws Exception 加载公钥时产生的异常
     */
    public RSAPublicKey loadPublicKey(InputStream keyIn) throws Exception {
        return loadPublicKey(keyInputStream2String(keyIn));
    }

    /**
     * 从raw资源文件夹加载公匙
     * @param context
     * @param id
     * @return
     * @throws Exception 加载公钥时产生的异常
     */
    public RSAPublicKey loadPublicKey(Context context, @RawRes int id) throws Exception {
        return loadPublicKey(context.getResources().openRawResource(id));
    }

    /**
     * 读取密匙输入流转换成字符串输出
     * <p>公匙/私匙通用过程
     * @param keyInputStream
     * @return
     * @throws IOException 密钥数据读取错误
     */
    private String keyInputStream2String(InputStream keyInputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(keyInputStream));
        StringBuilder key_sb = new StringBuilder();
        String readLine = null;
        while((readLine = br.readLine()) != null){
            if(readLine.charAt(0) == '-'){
                continue;
            }else{
                key_sb.append(readLine);
                key_sb.append('\r');
            }
        }
        return key_sb.toString();
    }

    /**
     * 从raw资源文件夹加载私匙
     * @param context
     * @param id
     * @return
     * @throws Exception 加载私钥时产生的异常
     */
    public RSAPrivateKey loadPrivateKey(Context context, @RawRes int id) throws Exception {
        return loadPrivateKey(context.getResources().openRawResource(id));
    }

    /**
     * 从文件中加载私钥
     * @return 是否成功
     * @throws Exception
     */
    public RSAPrivateKey loadPrivateKey(InputStream keyIn) throws Exception{
        return loadPrivateKey(keyInputStream2String(keyIn));
    }

    /**
     * 从字符串中加载私钥
     * @param privateKeyStr 私钥数据字符串
     * @throws InvalidKeySpecException 私钥非法
     * @throws Exception 私钥Base64解码错误
     */
    public RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
//        Key private_key = null;
        try {
            byte buffer[] = android.util.Base64.decode(privateKeyStr, android.util.Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            mKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "无此算法"); return null;
        }

        // 喺哩度初始化算法器
        if (mCipher == null) {
            try {
                mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "无此算法"); return null;
            } catch (NoSuchPaddingException e) {
                Log.e(TAG, "无此填充方式"); return null;
            }
        }
        return (RSAPrivateKey)mKey;
    }
}
