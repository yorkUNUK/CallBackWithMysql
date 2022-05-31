package com.example.callbackWithMysql.api.decrypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by whthomas on 15/6/23.
 */
public class AESCoder {

    public static final String KEY_ALGORITHM = "AES";

    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    // public static final String CIPHER_ALGORITHM = "AES";

    // 转换密钥
    private static Key toKey(byte[] key) {

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }

        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }
        secureRandom.setSeed(key);

        keyGenerator.init(128, secureRandom);

        // 实例化DES的材料
        SecretKey secretKey = keyGenerator.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    }

    /**
     * 解密
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        // 还原密钥
        Key translatedKey = toKey(key);
        try {
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, translatedKey);
            // 执行操作
            return cipher.doFinal(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String decryptAsString(byte[] data, byte[] key) {
        return new String(decrypt(data, key));
    }

    /**
     * 加密
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        // 还原密钥
        Key traslatedKey = toKey(key);
        try {
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, traslatedKey);
            // 执行操作
            return cipher.doFinal(data);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 生成密钥
     *
     * @return
     * @throws Exception
     */
    public static byte[] initKey() {

        // 实例化
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }

        // 要求密钥长度为256
        kg.init(256);

        // 生成秘密密钥
        SecretKey secretKey = kg.generateKey();

        // 获得秘密密钥
        return secretKey.getEncoded();
    }

}
