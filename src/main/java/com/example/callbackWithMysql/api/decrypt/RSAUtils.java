package com.example.callbackWithMysql.api.decrypt;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String CHARSET = "utf-8";

    private static String signSHA256RSA(String input, String privateKey) throws Exception {

        byte[] b1 = Base64.getDecoder().decode(privateKey.getBytes());

        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance("RSA");

            Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);

            privateSignature.initSign(kf.generatePrivate(new PKCS8EncodedKeySpec(b1)));

            privateSignature.update(input.getBytes("UTF-8"));

            byte[] s = privateSignature.sign();
            //先base64  在urlEncoder
            return URLEncoder.encode(Base64.getEncoder().encodeToString(s), CHARSET);

        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    public static boolean verifySHA256RSA(byte[] data, String sign, String publicKey) throws Exception {

        try {

            sign = URLDecoder.decode(sign, CHARSET);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PublicKey pubKey = keyFactory
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey.getBytes())));

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);

            signature.initVerify(pubKey);
            signature.update(data);

            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));

        } catch (Exception e) { // 签名数据失败
            throw new Exception(e);
        }

    }

    public static String signRSA(String content, String privateKey) throws Exception {
        return signSHA256RSA(content, privateKey);
    }

    public static boolean verifySHA256RSA(String data, String sign, String publicKey) throws Exception {

        try {
            return verifySHA256RSA(data.getBytes("UTF-8"), sign, publicKey);
        } catch (UnsupportedEncodingException e) {
            throw new Exception(e);
        }
    }

}
