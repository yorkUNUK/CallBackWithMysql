package com.example.callbackWithMysql.api.decrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author zhouzb
 * @company 杭州尚尚签网络科技有限公司
 * @date 2018/5/9 15:37
 * @since delta
 */
public class SSQAESEncrypt {

	public static final ConcurrentHashMap<String, SSQAESEncrypt> cache = new ConcurrentHashMap<>(1);
	private static final Charset CHARSET = Charset.forName("utf-8");
	private static final String KEY_ALGORITHM = "AES";

	private static final String CIPHER_ALGORITHM_CBC_NoPadding = "AES/CBC/NoPadding";
	private static final Integer AES_ENCODE_KEY_LENGTH = Integer.valueOf(43);
	private static final Integer RANDOM_LENGTH = Integer.valueOf(16);
	private static Logger logger = LoggerFactory.getLogger(SHA1Utils.class);
	/**
	 * aes密钥
	 */
	private byte[] aesKey;
	/**
	 * 申请是填写的
	 */
	private String token;
	/**
	 * 申请的oauth 客户id
	 */
	private String clientId;

	private SSQAESEncrypt(String token, String encodingAesKey, String clientId) {
		this.token = token;
		this.clientId = clientId;
		this.aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
	}

	private SSQAESEncrypt(String token, String encodingAesKey) {
		this.token = token;
		this.aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
	}

	public static SSQAESEncrypt instanceOf(String token, String encodingAesKey, String clientId) {
		if (null != token) {
			cache.putIfAbsent(token, new SSQAESEncrypt(token, encodingAesKey, clientId));
			return cache.get(token);
		}
		return null;
	}

	public static SSQAESEncrypt instanceOf(String token, String encodingAesKey) {
		if (null != token) {
			cache.putIfAbsent(token, new SSQAESEncrypt(token, encodingAesKey));
			return cache.get(token);
		}
		return null;
	}


	/**
	 * 产生64编码后的aes密钥
	 *
	 * @return
	 * @throws Exception
	 */
	public static String genKeyAES() throws Exception {

		KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);

		keyGen.init(256);
		SecretKey key = keyGen.generateKey();
		String base64Str = Base64.getEncoder().encodeToString(key.getEncoded());

		return base64Str;
	}

	// 生成4个字节的网络字节序
	public byte[] getNetworkBytesOrder(int sourceNumber) {
		byte[] orderBytes = new byte[4];
		orderBytes[3] = (byte) (sourceNumber & 0xFF);
		orderBytes[2] = (byte) (sourceNumber >> 8 & 0xFF);
		orderBytes[1] = (byte) (sourceNumber >> 16 & 0xFF);
		orderBytes[0] = (byte) (sourceNumber >> 24 & 0xFF);
		return orderBytes;
	}

	// 还原4个字节的网络字节序
	public int recoverNetworkBytesOrder(byte[] orderBytes) {
		int sourceNumber = 0;
		for (int i = 0; i < 4; i++) {
			sourceNumber <<= 8;
			sourceNumber |= orderBytes[i] & 0xff;
		}
		return sourceNumber;
	}

	/**
	 * aes 加密
	 *
	 * @param randomStr 随机数
	 * @param text      需加密的字符串
	 * @return
	 */
	public String encrypt(String randomStr, String text) {
		ByteGroup byteCollector = new ByteGroup();
		byte[] randomStrBytes = randomStr.getBytes(CHARSET);
		byte[] textBytes = text.getBytes(CHARSET);
		byte[] networkBytesOrder = getNetworkBytesOrder(textBytes.length);
		byte[] clientIdBytes = clientId.getBytes(CHARSET);

		// randomStr + networkBytesOrder + text + clientId
		byteCollector.addBytes(randomStrBytes);
		byteCollector.addBytes(networkBytesOrder);
		byteCollector.addBytes(textBytes);
		byteCollector.addBytes(clientIdBytes);

		// ... + pad: 使用自定义的填充方式对明文进行补位填充
		byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
		byteCollector.addBytes(padBytes);

		// 获得最终的字节流, 未加密
		byte[] unencrypted = byteCollector.toBytes();

		try {
			// 设置加密模式为AES的CBC模式
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);
			SecretKeySpec keySpec = new SecretKeySpec(aesKey, KEY_ALGORITHM);
			IvParameterSpec iv = new IvParameterSpec(aesKey, 0, RANDOM_LENGTH);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

			// 加密
			byte[] encrypted = cipher.doFinal(unencrypted);

			// 使用BASE64对加密后的字符串进行编码
			String base64Encrypted = Base64.getEncoder().encodeToString(encrypted);

			return base64Encrypted;
		} catch (Exception e) {
			logger.error("aes加密失败", e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 获取签名
	 *
	 * @param token      申请是填写的token值
	 * @param timestamp  时间戳
	 * @param nonce      随机数
	 * @param encryptMsg 加密后的信息
	 * @return
	 */
	public String getSignature(String token, String type, String timestamp, String nonce, String encryptMsg) {

		return SHA1Utils.getSHA1(token, type, timestamp, nonce, encryptMsg);
	}

	/**
	 * 解密：包括签名校验
	 *
	 * @param msgSignature 签名时间参数
	 * @param timestamp    时间戳
	 * @param nonce        随机数
	 * @param encryptMsg   加密后的信息
	 * @return
	 */
	private String getDecryptMsg(String msgSignature, String type, String timestamp, String nonce, String encryptMsg) {

		String signature = SHA1Utils.getSHA1(token, type, timestamp, nonce, encryptMsg);
		if (!signature.equals(msgSignature)) {
			throw new IllegalStateException("signatures do not match");
		} else {
			String result = this.decrypt(encryptMsg);
			return result;
		}

	}

	public String decrypt(String text) {
		DecryptedContentAndClientId decryptedContentAndClientId = decryptReturnValueContainsClientId(text);

		// client不相同的情况
		if (!decryptedContentAndClientId.getFromClientId().equals(clientId)) {
			throw new IllegalStateException("clients do not match from clientId " + decryptedContentAndClientId.getFromClientId() + " ,actual clientId " + clientId);
		}
		return decryptedContentAndClientId.getPlanContent();

	}

	public DecryptedContentAndClientId decryptReturnValueContainsClientId(String text) {
		byte[] original;
		try {
			// 设置解密模式为AES的CBC模式
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);
			SecretKeySpec key_spec = new SecretKeySpec(aesKey, KEY_ALGORITHM);
			IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, RANDOM_LENGTH));
			cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

			// 使用BASE64对密文进行解码
			byte[] encrypted = Base64.getDecoder().decode(text);

			// 解密
			original = cipher.doFinal(encrypted);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		String planContent, fromClientId;
		try {
			// 去除补位字符
			byte[] bytes = PKCS7Encoder.decode(original);

			// 分离16位随机字符串,网络字节序和AppId
			byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);

			int plainTextLength = recoverNetworkBytesOrder(networkOrder);

			planContent = new String(Arrays.copyOfRange(bytes, 20, 20 + plainTextLength), CHARSET);
			fromClientId = new String(Arrays.copyOfRange(bytes, 20 + plainTextLength, bytes.length),
					CHARSET);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		DecryptedContentAndClientId decryptedContentAndClientId = new DecryptedContentAndClientId(planContent, fromClientId);
		return decryptedContentAndClientId;

	}

	public String getRandomStr() {
		String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 16; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static class DecryptedContentAndClientId {
		private String planContent;
		private String fromClientId;

		public DecryptedContentAndClientId(String planContent, String fromClientId) {
			this.planContent = planContent;
			this.fromClientId = fromClientId;
		}

		public String getPlanContent() {
			return planContent;
		}

		public void setPlanContent(String planContent) {
			this.planContent = planContent;
		}

		public String getFromClientId() {
			return fromClientId;
		}

		public void setFromClientId(String fromClientId) {
			this.fromClientId = fromClientId;
		}
	}

	public static class ByteGroup {
		ArrayList<Byte> byteContainer = new ArrayList<>();

		public byte[] toBytes() {
			byte[] bytes = new byte[byteContainer.size()];
			for (int i = 0; i < byteContainer.size(); i++) {
				bytes[i] = byteContainer.get(i);
			}
			return bytes;
		}

		public ByteGroup addBytes(byte[] bytes) {
			for (byte b : bytes) {
				byteContainer.add(b);
			}
			return this;
		}

		public int size() {
			return byteContainer.size();
		}
	}

	public static class PKCS7Encoder {
		static Charset CHARSET = Charset.forName("utf-8");
		static int BLOCK_SIZE = 32;

		/**
		 * 获得对明文进行补位填充的字节.
		 *
		 * @param count 需要进行填充补位操作的明文字节个数
		 * @return 补齐用的字节数组
		 */
		static byte[] encode(int count) {
			// 计算需要填充的位数
			int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
			if (amountToPad == 0) {
				amountToPad = BLOCK_SIZE;
			}
			// 获得补位所用的字符
			char padChr = chr(amountToPad);
			String tmp = new String();
			for (int index = 0; index < amountToPad; index++) {
				tmp += padChr;
			}
			return tmp.getBytes(CHARSET);
		}

		/**
		 * 删除解密后明文的补位字符
		 *
		 * @param decrypted 解密后的明文
		 * @return 删除补位字符后的明文
		 */
		static byte[] decode(byte[] decrypted) {
			int pad = (int) decrypted[decrypted.length - 1];
			if (pad < 1 || pad > 32) {
				pad = 0;
			}
			return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
		}

		/**
		 * 将数字转化成ASCII码对应的字符，用于对明文进行补码
		 *
		 * @param a 需要转化的数字
		 * @return 转化得到的字符
		 */
		static char chr(int a) {
			byte target = (byte) (a & 0xFF);
			return (char) target;
		}
	}

}
