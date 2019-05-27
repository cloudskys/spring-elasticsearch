package com.fxwiz.base.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 非对称RSA加密解密工具<br>
 * <p>
 * <li>对称加密
 * <P>
 * 对称加密称为密钥加密，速度快，但加密和解密的钥匙必须相同，只有通信双方才能知道密钥，常见的有DES，3DES，AES对称加密。
 * DES密钥长度是8，AES密钥长度是[16, 24, 32]
 * </P>
 * </li>
 * <li>非对称加密
 * <P>
 * 非对称加密称为公钥加密，算法更加复杂，速度慢，加密和解密钥匙不相同，任何人都可以知道公钥，只有一个人持有私钥可以解密。常见的就是RSA了。
 * </P>
 * </li>
 * <li>不可逆加密
 * <P>
 * 典型的代表就是MD5加密了。
 * </P>
 * </li>
 * </p>
 * 
 * @author xuyd
 * 
 */
public class AlgorithmRSA {

	protected static final Logger logger = LoggerFactory.getLogger(AlgorithmRSA.class);

	// RSA加密，对于C预览没有RSA字符串，而是RSA/ECB/NoPadding
	public static final String ALGORITHM_NAME = "RSA";
	// public static final String ALGORITHM_NAME = "RSA/ECB/NoPadding"; //
	// jdk1.8+

	public static AlgorithmRSA _self = null;
	
	/**
	 * RSA密钥长度必须是64的倍数，在512~16384之间。默认是1024位，则密钥长度是128字节，加密时候密文长度不能超过128-11
	 */
	public int KEY_SIZE = 1024;

	/**
	 * RSA最大加密明文大小，BouncyCastle提供的加密算法能够支持到的RSA明文长度最长为密钥长度
	 */
	private int MAX_ENCRYPT_BLOCK = KEY_SIZE / 8 - 11;
	
	/**
	 * RSA最大解密密文大小
	 */
	private int MAX_DECRYPT_BLOCK = KEY_SIZE / 8;
	


	// map存储公钥的key
	public static final String PUBLIC_KEY = "publicKey.key";

	// map存储私钥的key
	public static final String PRIVATE_KEY = "privateKey.key";

	// 公钥文件名
	public static final String PUBLIC_KEY_FILE_NAME = "public.key";

	// 私钥文件名
	public static final String PRIVATE_KEY_FILE_NAME = "private.key";

	/********************************* 生成密钥(S) ****************************************/

	private AlgorithmRSA(){
	}

	/**
	 * 单例获取对象，密钥长度默认
	 */
	public static AlgorithmRSA getInstance() {
		if(_self == null) {
			_self = new AlgorithmRSA();
		}
		return _self;
	}
	
	/**
	 * 密钥长度设置
	 * 
	 * @param keySize
	 */
	public AlgorithmRSA setKeySize(int keySize) {
		KEY_SIZE = keySize;
		MAX_ENCRYPT_BLOCK = KEY_SIZE / 8 - 11;
		MAX_DECRYPT_BLOCK = KEY_SIZE / 8;
		return this;
	}
	
	/**
	 * 工具中只调用一次，生成密钥并写入文件
	 * 
	 * @param rootPath
	 *            文件存放根目录
	 */
	public boolean generateKeyIntoFile(String rootPath) {
		Map<String, String> keyMap = generateKeyString();
		// 保存公钥
		if(writeKeyIntoFile(keyMap.get(PUBLIC_KEY), rootPath + PUBLIC_KEY_FILE_NAME) && 
				writeKeyIntoFile(keyMap.get(PRIVATE_KEY), rootPath + PRIVATE_KEY_FILE_NAME)){
			return true;
		}
		return false;
	}
	
	/**
	 * 生成指定长度的密钥字符串
	 * 
	 * @return
	 */
	public Map<String, String> generateKeyString() {
		Map<String, String> keyMap = new HashMap<String, String>();
		sun.misc.BASE64Encoder base64Encoder = new sun.misc.BASE64Encoder();
		// 获取密钥对
		Map<String, byte[]> keyPairs = generateKeyBytes();
		// 保存公钥
		byte[] publicKeyBytes = keyPairs.get(PUBLIC_KEY);
		String pk = base64Encoder.encode(publicKeyBytes);
		keyMap.put(PUBLIC_KEY, pk);
		
		// 保存私钥
		byte[] privateKeyBytes = keyPairs.get(PRIVATE_KEY);
		pk = base64Encoder.encode(privateKeyBytes);
		keyMap.put(PRIVATE_KEY, pk);
		return keyMap;
	}
	
	/**
	 * 生成密钥对字节码
	 * 
	 * @return
	 */
	public Map<String, byte[]> generateKeyBytes() {
		Map<String, byte[]> keyPairs = new HashMap<String, byte[]>();
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_NAME);
			// 初始化密钥长度
			keyPairGenerator.initialize(KEY_SIZE);
			KeyPair keyPare = keyPairGenerator.generateKeyPair();
			// 获取公钥
			PublicKey publicKey = keyPare.getPublic();
			// 获取私钥
			PrivateKey privateKey = keyPare.getPrivate();

			// 返回密钥字节码
			keyPairs.put(PUBLIC_KEY, publicKey.getEncoded());
			keyPairs.put(PRIVATE_KEY, privateKey.getEncoded());
		} catch (Exception e) {
			logger.error("generator public/private key failed.", e);
		}
		return keyPairs;
	}

	/**
	 * 密钥保存到文件中
	 * 
	 * @param keys
	 * @param filePath
	 * 
	 */
	private static boolean writeKeyIntoFile(String keys, String filePath) {
		File publicKeyFile = new File(filePath);
		OutputStream os = null;
		try {
			// 目录不存在创建
			File fp = new File(new File(filePath).getParent());
			if (!fp.exists()) {
				fp.mkdirs();
			}
			os = new FileOutputStream(publicKeyFile);
			os.write(keys.getBytes("utf-8"));
			return true;
		} catch (Exception e) {
			logger.error("write key into file failed.", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

	/********************************* 生成密钥(E) ****************************************/

	/********************************* 解析密钥(S) ****************************************/

	/**
	 * 读取key
	 * 
	 * @param filePath
	 * @return
	 */
	public String readFromFile(String filePath) {
		byte[] keyBytes = readIntoByte(filePath);
		String key = new String(keyBytes);
		return key;
	}

	/**
	 * 读取key文件到内存中
	 * 
	 * @param filePath
	 */
	private byte[] readIntoByte(String filePath) {
		File publicKeyFile = new File(filePath);
		InputStream is = null;
		try {
			is = new FileInputStream(publicKeyFile);
			ByteArrayOutputStream arrayOS = new ByteArrayOutputStream();
			byte[] keys = new byte[1024];
			int len = -1;
			while ((len = is.read(keys)) != -1) {
				arrayOS.write(keys, 0, len);
			}
			return arrayOS.toByteArray();
		} catch (Exception e) {
			logger.error("read key from file failed.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	/********************************* 解析密钥(E) ****************************************/

	/********************************* 加密解密处理(S) ************************************/

	/**
	 * 加密处理
	 * 
	 * @param val
	 * @param publicKey
	 *            公钥密钥
	 * @return
	 */
	public String encrypt(String val, String publicKey) {
		if (val == null || val.length() <= 0) {
			return null;
		}

		BASE64Encoder base64Encoder = new BASE64Encoder();
		BASE64Decoder base64Decoder = new BASE64Decoder();

		try {
			byte[] publicKeyBytes = base64Decoder.decodeBuffer(publicKey);
			// 还原公钥
			PublicKey pk = restorePublicKey(publicKeyBytes);
			// 公钥加密
			byte[] encryptBytes = _encrypt(pk, val.getBytes("utf-8"));
			String strEncrypt = base64Encoder.encode(encryptBytes);
			return strEncrypt;
		} catch (Exception e) {
			logger.error("encrypt failed.", e);
		}
		return null;
	}

	/**
	 * 解密处理
	 * 
	 * @param val
	 * @param privateKey
	 *            私钥密钥
	 * 
	 * @return
	 */
	public String decrypt(String decodeVal, String privateKey) {
		if (decodeVal == null || decodeVal.length() <= 0) {
			return null;
		}
		BASE64Decoder baseDe64 = new BASE64Decoder();

		try {
			byte[] privateKeyBytes = baseDe64.decodeBuffer(privateKey);
			// 还原私钥
			PrivateKey pk = restorePrivateKey(privateKeyBytes);
			// 解密
			byte[] decryptBytes = _decrypt(pk, baseDe64.decodeBuffer(decodeVal));
			String strDecrypt = new String(decryptBytes);
			return strDecrypt;
		} catch (Exception e) {
			logger.error("decrypt failed.", e);
		}
		return null;
	}

	/**
	 * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
	 * 
	 * @param keyBytes
	 * @return
	 */
	private PublicKey restorePublicKey(byte[] keyBytes) {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(ALGORITHM_NAME);
			PublicKey pk = factory.generatePublic(keySpec);
			return pk;
		} catch (Exception e) {
			logger.error("restore public key failed.", e);
		}
		return null;
	}

	/**
	 * 还原私钥，PKCS8EncodedKeySpec 用于构建私钥的规范
	 * 
	 * @param keyBytes
	 * @return
	 */
	private PrivateKey restorePrivateKey(byte[] keyBytes) {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(ALGORITHM_NAME);
			PrivateKey pk = factory.generatePrivate(keySpec);
			return pk;
		} catch (Exception e) {
			logger.error("restore private key failed.", e);
		}
		return null;
	}

	/**
	 * 加密处理
	 * 
	 * @param publicKey
	 *            公钥
	 * @param dataBytes
	 *            原始数据
	 * 
	 * @return 加密后的字节
	 */
	private byte[] _encrypt(Key key, byte[] dataBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		int inputLen = dataBytes.length;
		int offset = 0;
		int i = 0;
		byte[] cache;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (inputLen > offset) {
			if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
				// 块的倍数
				cache = cipher.doFinal(dataBytes, offset, MAX_ENCRYPT_BLOCK);
			} else {
				// 到达最后一段
				cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
			}
			out.write(cache);
			i++;
			offset = i * MAX_ENCRYPT_BLOCK;
		}
		out.close();
		byte[] encryptedData = out.toByteArray();
		return encryptedData;
	}

	/**
	 * 加密处理
	 * 
	 * @param publicKey
	 *            公钥
	 * @param data
	 *            加密后的数据
	 * 
	 * @return 加密后的字节
	 */
	private byte[] _decrypt(Key key, byte[] decodeData) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
		cipher.init(Cipher.DECRYPT_MODE, key);

		int inputLen = decodeData.length;
		int offset = 0;
		int i = 0;
		byte[] cache;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (inputLen > offset) {
			if (inputLen - offset > MAX_DECRYPT_BLOCK) {
				// 块的倍数
				cache = cipher.doFinal(decodeData, offset, MAX_DECRYPT_BLOCK);
			} else {
				// 到达最后一段
				cache = cipher.doFinal(decodeData, offset, inputLen - offset);
			}
			out.write(cache);
			i++;
			offset = i * MAX_DECRYPT_BLOCK;
		}
		out.close();
		byte[] decryptedData = out.toByteArray();
		return decryptedData;
	}

	
	/********************************* 加密解密处理(S) ************************************/

	/**
	 * TEST
	 * 
	 * @param args
	 */
	public static void main1(String[] args) {
		String orgVal = "MANUTD is 原始字符串the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatestMANUTD is the greatest";
		String rootPath = "C:/home/hefa/";
		
		AlgorithmRSA algorithmRSA = AlgorithmRSA.getInstance();
		// 生成密钥对
		algorithmRSA.generateKeyIntoFile(rootPath);

		// 读取密钥
		String publicKey = algorithmRSA.readFromFile(rootPath + AlgorithmRSA.PUBLIC_KEY_FILE_NAME);

		String privateKey = algorithmRSA.readFromFile(rootPath + AlgorithmRSA.PRIVATE_KEY_FILE_NAME);
		System.out.println("=====RSA读取公钥=====\n" + publicKey);
		System.out.println("=====RSA读取私钥=====\n" + privateKey);
		
		// 加密
		String enVal = algorithmRSA.encrypt(orgVal, publicKey);
		System.out.println("=====RSA加密原文=====\n" + orgVal);
		System.out.println("=====RSA加密密钥=====\n" + publicKey);
		System.out.println("=====RSA加密密文=====\n" + enVal);

		// 解密
		String decryptedVal = algorithmRSA.decrypt(enVal, privateKey);
		System.out.println("=====RSA解密密文=====\n" + enVal);
		System.out.println("=====RSA解密密钥=====\n" + privateKey);
		System.out.println("=====RSA解密原文=====\n" + decryptedVal);
	}

}
