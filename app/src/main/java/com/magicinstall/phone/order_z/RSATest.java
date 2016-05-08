package com.magicinstall.phone.order_z;

import android.util.Base64;

import java.io.BufferedReader;
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

/**
 * Created by wing on 16/5/7.
 */
public class RSATest {
    Key key = null;

//    public RSATest (Context context) throws Exception {
//        String text = "~!@#$%^&*()_+`1234567890-=" +
//                        "QWERTYUIOP{}|qwertyuiop[]\\" +
//                        "ASDFGHJKL:\"asdfghjkl;'" +
//                        "ZXCVBNM<>?zxcvbnm,./'";
//
//        // 私匙加的密
//        String private_encode =
//                "b8a0a726bee264fa8050e52fe8d712e47efbbe83" +
//                "f0a34f0eac5dbe4e3c74cf9e6568663b81485157" +
//                "e00fc611da5469e61c52f49dd13af523930f7c2e" +
//                "757e7b693d42a21863ce925aeeaa780dd8461c19" +
//                "2146328b1239481af6adc5cb2d117ca434ded3a3" +
//                "f08650705ae5918d82258b5e9ff58612fdab4d08" +
//                "5c5b70595dd58aa4";
//
//        byte[] data = Hash.hexString2Bytes(private_encode);
//
//        // 载入公匙
//        RSAPublicKey public_key = loadPublicKey(
//                context.getResources().openRawResource(R.raw.android_public_key));
//
//        // 载入私匙
//        RSAPrivateKey private_key = loadPrivateKey(
//                context.getResources().openRawResource(R.raw.android_private_key));
//
//
//        Log.d("输入", Hash.bytes2Hex(data));
//
////        byte[] public_Data = encrypt(private_key, data);
////        Log.d("私匙加密", Hash.bytes2Hex(public_Data));
////
//        byte[] private_data = decrypt(public_key, data);
//        Log.d("公匙解密", Hash.bytes2Hex(private_data));
//
////        byte[] public_Data = encrypt(public_key, data);
//
//
////        byte[] public_Data = encrypt(public_key, data);
////        Log.d("公匙加密", Hash.bytes2Hex(public_Data));
//
////        byte[] private_data = decrypt(private_key, data);
////        Log.d("私匙解密", Hash.bytes2Hex(private_data));
//
//        String p = new String(private_data);
//        Log.d("转字符串", p);
//    }

    /************************************************************************
     *                              计算过程                                 *
     ************************************************************************/

    /**
     * 解密过程
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public byte[] decrypt(/*RSAPrivate*/Key privateKey, byte[] cipherData) throws Exception{
        if (privateKey== null){
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher= null;
        try {
            cipher= Cipher.getInstance("RSA/ECB/PKCS1Padding");//, new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output= cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * 加密过程
     * @param publicKey 公钥
     * @param plainTextData 明文数据
     * @return
     * @throws Exception 加密过程中的异常信息
     */
    public byte[] encrypt(/*RSAPublic*/Key publicKey, byte[] plainTextData) throws Exception{
        if(publicKey== null){
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher= null;
        try {
            cipher= Cipher.getInstance("RSA/ECB/PKCS1Padding");//, new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output= cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /************************************************************************
     *                              密匙加载                                 *
     ************************************************************************/

    /**
     * 从字符串中加载公钥
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception{
        RSAPublicKey publicKey;
        byte[] buffer= Base64.decode(publicKeyStr, Base64.DEFAULT);

        try {
            X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");
            publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
//        } catch (IOException e) {
//            throw new Exception("公钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }


//        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
//        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        return publicKey;
    }

    /**
     * 从文件中输入流中加载公钥
     * @param keyIn 公钥输入流
     * @throws Exception 加载公钥时产生的异常
     */
    public RSAPublicKey loadPublicKey(InputStream keyIn) throws Exception{
//        StringBuilder sb;
//        try {
//            BufferedReader br= new BufferedReader(new InputStreamReader(in));
//            String readLine= null;
//            sb= new StringBuilder();
//            while((readLine= br.readLine())!=null){
//                if(readLine.charAt(0)=='-'){
//                    continue;
//                }else{
//                    sb.append(readLine);
//                    sb.append('\r');
//                }
//            }
//            loadPublicKey(sb.toString());
//        } catch (IOException e) {
//            throw new Exception("公钥数据流读取错误");
//        } catch (NullPointerException e) {
//            throw new Exception("公钥输入流为空");
//        }
        return loadPublicKey(keyInputStream2String(keyIn));
    }

    /**
     * 读取密匙输入流转换成字符串输出
     * @param keyInputStream
     * @return
     * @throws Exception
     */
    private String keyInputStream2String(InputStream keyInputStream) throws Exception {
        StringBuilder key_sb;
        try {
            BufferedReader br= new BufferedReader(new InputStreamReader(keyInputStream));
            String readLine= null;
            key_sb = new StringBuilder();
            while((readLine = br.readLine()) != null){
                if(readLine.charAt(0) == '-'){
                    continue;
                }else{
                    key_sb.append(readLine);
                    key_sb.append('\r');
                }
            }
        } catch (IOException e) {
            throw new Exception("密钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("密钥输入流为空");
        }
        return key_sb.toString();
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
     * @throws Exception 加载私钥时产生的异常
     */
    public RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception{
        try {
            byte[] buffer= base64.decryptBASE64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (IOException e) {
            throw new Exception("私钥Base64解码错误");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }
}
