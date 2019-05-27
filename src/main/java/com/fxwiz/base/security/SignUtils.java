package com.fxwiz.base.security;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * 绛惧悕宸ュ叿
 * 
 * @author xuyd
 * 
 */
public class SignUtils {

	protected static final Logger logger = LoggerFactory.getLogger(SignUtils.class);

	/**
	 * 绛惧悕涔嬪悗淇濆瓨鐨勯敭鍊煎锛氶敭
	 */
	public final static String SIGNATURE_KEY = "sign";
	/**
	 * 浠ｇ鍚嶅瓧绗︿覆
	 */
	public final static String CONTENT_KEY = "content";
	/**
	 * 寰呯鍚嶅瓧绗︿覆锛坋ncode澶勭悊鍚庯級
	 */
	public final static String CONTENT_ENCODE_KEY = "encodeContent";

	/**
	 * 瀵逛簬鏀粯绫荤殑绛惧悕锛欰PI鐨勬敮浠樺瘑閽ey闇�瑕佹斁鍒版渶鍚�
	 */
	public final static String SIGNATURE_FIELD_KEY = "key";

	/**
	 * MD5绛惧悕绫诲瀷
	 */
	public final static String SIGN_TYPE_MD5 = "MD5";

	/**
	 * RSA绛惧悕绫诲瀷
	 */
	public final static String SIGN_TYPE_RSA = "RSA";
	/**
	 * RSA1绛惧悕绫诲瀷
	 */
	public final static String SIGN_TYPE_RSA1 = "RSA1";
	/**
	 * RSA2绛惧悕绫诲瀷
	 */
	public final static String SIGN_TYPE_RSA2 = "RSA2";
	/**
	 * SHA1绛惧悕绫诲瀷
	 */
	public final static String SIGN_TYPE_SHA1 = "SHA-1";

	/**
	 * 鏅�氶獙璇佺鍚�
	 * e.g:nonce=91c15e34aab54176bcc5b34372ab8d63&timestamp=1503915005&signature=ba1d41306bbf164b2ea5690886e99b6cfb61abca
	 * @param nonce
	 * @param timestamp
	 * @param signature
	 * @return
	 */
	private static final String CACHEKEY_TOKEN = "b4fc7819b3bc4042b6e5d05d63203f43";
	public static boolean checkSimpleSign(String nonce, String timestamp, String signature) {
		if (timestamp == null || nonce == null || signature == null) {
			return false;
		}
		String[] array = new String[] { timestamp, nonce, CACHEKEY_TOKEN};
		StringBuilder content = new StringBuilder();

		// 瀛楃涓叉帓搴�
		java.util.Arrays.sort(array);
		for (int i = 0; i < array.length; i++) {
			content.append(array[i]);
		}
		// SHA1绛惧悕楠岃瘉
		boolean ret = SignUtils.checkSignSHA1(signature, content.toString());
		if (ret) {
			logger.info("绛惧悕楠岃瘉鎴愬姛");
		} else {
			logger.error("绛惧悕楠岃瘉澶辫触");
		}
		return ret;
	}

	/**
	 * 鍒ゆ柇宸茬粡绛惧悕鍚庣殑鍊硷紝鍜岄渶瑕佽绛惧悕鐨凨EY鐨勭鍚嶆槸鍚︿竴鑷淬��
	 * 
	 * @param sign
	 *            绛惧悕鐨勫��
	 * @param content
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @return 鍓嶅悗绛惧悕涓�鑷达細true...else:false
	 */
	public static boolean checkSignSHA1(String sign, String content) {
		if (sign == null || sign.length() <= 0) {
			return false;
		}
		String newSign  = signSHA1(content);
		return sign.toUpperCase().equals(newSign.toUpperCase());
	}

	/**
	 * 鍒ゆ柇宸茬粡绛惧悕鍚庣殑鍊硷紝鍜岄渶瑕佽绛惧悕鐨凨EY鐨勭鍚嶆槸鍚︿竴鑷淬��
	 * 
	 * @param sign
	 *            绛惧悕鐨勫��
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @return 鍓嶅悗绛惧悕涓�鑷达細true...else:false
	 */
	public static boolean checkSignSHA1(String sign, SignParameter signParameter) {
		if (sign == null || sign.length() <= 0) {
			return false;
		}
		Map<String, String> map = signSHA1(signParameter);
		String newSign = map.get(SIGNATURE_KEY);
		return sign.toUpperCase().equals(newSign.toUpperCase());
	}

	/******************************** SIGN SHA1 (S) ****************************************/

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @return
	 * @se
	 */
	public static Map<String, String> signSHA1(SignParameter signParameter) {
		return signSHA1(signParameter, null, false);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * 
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @return
	 */
	public static Map<String, String> signSHA1(SignParameter signParameter, String encodeCharset, boolean useEncode) {
		return _sign(SIGN_TYPE_SHA1, signParameter, encodeCharset, useEncode);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param content
	 *            绛惧悕鍐呭
	 * @return
	 */
	public static String signSHA1(String content) {
		return _sign(SIGN_TYPE_SHA1, content);
	}

	/******************************** SIGN SHA1 (S) ****************************************/

	/**
	 * 鍒ゆ柇宸茬粡绛惧悕鍚庣殑鍊硷紝鍜岄渶瑕佽绛惧悕鐨凨EY鐨勭鍚嶆槸鍚︿竴鑷淬��
	 * 
	 * @param sign
	 *            绛惧悕鐨勫��
	 * @param content
	 *            寰呴獙璇佺殑鍐呭
	 * @return 鍓嶅悗绛惧悕涓�鑷达細true...else:false
	 */
	public static boolean checkSignMD5(String content, String sign) {
		if (sign == null || sign.length() <= 0) {
			return false;
		}
		String newSign = signMD5(content);
		return sign.toUpperCase().equals(newSign.toUpperCase());
	}

	/******************************** SIGN MD5 (S) ****************************************/

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @return
	 * @see #signMD5(SignParameter, String)
	 */
	public static Map<String, String> signMD5(SignParameter signParameter) {
		return signMD5(signParameter, null, false);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * 
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @return
	 */
	public static Map<String, String> signMD5(SignParameter signParameter, String encodeCharset, boolean useEncode) {
		return _sign(SIGN_TYPE_MD5, signParameter, encodeCharset, useEncode);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆MD5绛惧悕<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @return
	 */
	public static String signMD5(String content) {
		return _sign(SIGN_TYPE_MD5, content);
	}

	/******************************** SIGN MD5 (E) ****************************************/

	/**
	 * 楠岃瘉RSA绛惧悕鏈夋晥鎬э紝鐢ㄥ叕閽ラ獙璇乧ontent鍐呭鐨勭鍚嶇粨鏋滄槸鍚︿负sign
	 * 
	 * @param content
	 *            鍘熷鍐呭
	 * @param sign
	 *            绛惧悕鍚庡唴瀹�
	 * @param publicKey
	 *            RSA鍏挜
	 * @return
	 */
	public static boolean checkSignRSA_MD5(String content, String sign, String publicKey) {
		return _checkSignRSA(content, sign, publicKey, "MD5WithRSA");
	}

	/**
	 * 楠岃瘉RSA绛惧悕鏈夋晥鎬э紝鐢ㄥ叕閽ラ獙璇乧ontent鍐呭鐨勭鍚嶇粨鏋滄槸鍚︿负sign
	 * 
	 * @param content
	 *            鍘熷鍐呭
	 * @param sign
	 *            绛惧悕鍚庡唴瀹�
	 * @param publicKey
	 *            RSA鍏挜
	 * @return
	 */
	public static boolean checkSignRSA_SHA1(String content, String sign, String publicKey) {
		return _checkSignRSA(content, sign, publicKey, "SHA1WithRSA");
	}

	/**
	 * 楠岃瘉RSA绛惧悕鏈夋晥鎬э紝鐢ㄥ叕閽ラ獙璇乧ontent鍐呭鐨勭鍚嶇粨鏋滄槸鍚︿负sign
	 * 
	 * @param content
	 *            鍘熷鍐呭
	 * @param sign
	 *            绛惧悕鍚庡唴瀹�
	 * @param publicKey
	 *            RSA鍏挜
	 * @return
	 */
	public static boolean checkSignRSA_SHA256(String content, String sign, String publicKey) {
		return _checkSignRSA(content, sign, publicKey, "SHA256WithRSA");
	}

	/**
	 * 楠岃瘉RSA绛惧悕鏈夋晥鎬э紝鐢ㄥ叕閽ラ獙璇乧ontent鍐呭鐨勭鍚嶇粨鏋滄槸鍚︿负sign
	 * 
	 * @param content
	 *            鍘熷鍐呭
	 * @param sign
	 *            绛惧悕鍚庡唴瀹�
	 * @param signKey
	 *            RSA鍏挜
	 * @param signAlgorithmName
	 *            绛惧悕绠楁硶鍚嶇О
	 * @return
	 */
	private static boolean _checkSignRSA(String content, String sign, String signKey, String signAlgorithmName) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
			byte[] encodedKey = Base64.decode(signKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			java.security.Signature signature = java.security.Signature.getInstance(signAlgorithmName);
			signature.initVerify(pubKey);
			signature.update(content.getBytes("utf-8"));
			boolean bverify = signature.verify(Base64.decode(sign));
			return bverify;
		} catch (Exception e) {
			logger.error("楠岃瘉RSA绛惧悕澶辫触銆�", e);
		}
		return false;
	}

	/******************************** SIGN RSA (S) ****************************************/

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static Map<String, String> signRSA_MD5(SignParameter signParameter, String privateKey) {
		return signRSA_MD5(signParameter, null, false, privateKey);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static Map<String, String> signRSA_MD5(SignParameter signParameter, String encodeCharset, boolean useEncode, String privateKey) {
		return _signRSA(SIGN_TYPE_RSA, signParameter, encodeCharset, useEncode, privateKey, "MD5WithRSA");
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param content
	 *            绛惧悕鍐呭
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static String signRSA_MD5(String content, String privateKey) {
		return _signRSA(SIGN_TYPE_RSA, content, privateKey, "MD5WithRSA");
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 * @see #signRSA_SHA1(SignParameter, String, String)
	 */
	public static Map<String, String> signRSA_SHA1(SignParameter signParameter, String privateKey) {
		return signRSA_SHA1(signParameter, null, false, privateKey);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static Map<String, String> signRSA_SHA1(SignParameter signParameter, String encodeCharset, boolean useEncode, String privateKey) {
		return _signRSA(SIGN_TYPE_RSA, signParameter, encodeCharset, useEncode, privateKey, "SHA1WithRSA");
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param content
	 *            绛惧悕鍐呭
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static String signRSA_SHA1(String content, String privateKey) {
		return _signRSA(SIGN_TYPE_RSA, content, privateKey, "SHA1WithRSA");
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @param encodeCharset
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 * @see SignUtils#signsignRSA_SHA256
	 */
	public static Map<String, String> signRSA_SHA256(SignParameter signParameter, String privateKey) {
		return signRSA_SHA256(signParameter, null, false, privateKey);
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param signParameter
	 *            璁剧疆鍙傛暟鐨勭殑瀵硅薄
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static Map<String, String> signRSA_SHA256(SignParameter signParameter, String encodeCharset, boolean useEncode, String privateKey) {
		return _signRSA(SIGN_TYPE_RSA, signParameter, encodeCharset, useEncode, privateKey, "SHA256WithRSA");
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆RSA绛惧悕锛屾墍鏈夊弬鏁伴兘闇�瑕佹墜鍔ㄨ缃�<br>
	 * 
	 * @param content
	 *            绛惧悕鍐呭
	 * @param privateKey
	 *            鍔犲瘑绉侀挜
	 * @return
	 */
	public static String signRSA_SHA256(String content, String privateKey) {
		return _signRSA(SIGN_TYPE_RSA, content, privateKey, "SHA256WithRSA");
	}

	/******************************** SIGN RSA (E) ****************************************/

	/****************************************** 绛惧悕(S) ******************************************************/

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕<br>
	 * 鍙瀹炵幇鎺ュ彛ISign鎺ュ彛锛岄渶瑕佺鍚嶇殑涓茬敤閿�煎璁剧疆鍒版柟娉曞弬鏁颁腑銆�<br>
	 * 鏂规硶浼氳嚜鍔ㄦ帓搴忥紝绛惧悕銆�<br>
	 * 
	 * @param digest
	 *            MD5,SHA-1
	 * @param signParameter
	 *            鍙傛暟
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @return
	 */
	private static Map<String, String> _sign(String digest, SignParameter signParameter, String encodeCharset, boolean useEncode) {
		// 鎷兼帴鎴怘TML鐨勫弬鏁版帴鍙�
		// 娉ㄦ剰杩欓噷鍙傛暟鍚嶅繀椤诲叏閮ㄥ皬鍐欙紝涓斿繀椤绘湁搴�
		Map<String, String> params = signParameter.getParams();
		signParameter.initParams(params);
		String content = concatMap2Html(params, encodeCharset, useEncode);
		String signature = _sign(digest, content);
		params.put(SIGNATURE_KEY, signature);
		return params;
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕<br>
	 * 鏂规硶浼氳嚜鍔ㄦ帓搴忥紝绛惧悕銆�<br>
	 * 
	 * @param digest
	 *            MD5,SHA-1
	 * @param content
	 *            寰呯鍚嶇殑鍐呭
	 * @return
	 */
	private static String _sign(String digest, String content) {
		String signType = digest;
		try {
			MessageDigest crypt = MessageDigest.getInstance(digest);
			crypt.reset();
			crypt.update(content.getBytes("UTF-8"));
			byte[] byteDigest = crypt.digest();
			String signature = byteToHex(byteDigest);
			logger.debug("\n" + signType + "绛惧悕瀛楃涓诧細" + content + "\n" + signType + "绛惧悕缁撴灉锛�" + signature);
			return signature;
		} catch (Exception e) {
			logger.error(signType + "绛惧悕澶辫触銆�", e);
		}
		return null;
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕<br>
	 * 
	 * @param digest
	 *            RSA
	 * @param signParameter
	 *            寰呯鍚嶇殑鍐呭
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param useEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @param signKey
	 *            绛惧悕瀵嗛挜
	 * @param signAlgorithmName
	 *            鏍囧噯绛惧悕绠楁硶鍚嶇О
	 * @return
	 */
	private static Map<String, String> _signRSA(String digest, SignParameter signParameter, String encodeCharset, boolean useEncode,
			String signKey, String signAlgorithmName) {
		// 鎷兼帴鎴怘TML鐨勫弬鏁版帴鍙�
		// 娉ㄦ剰杩欓噷鍙傛暟鍚嶅繀椤诲叏閮ㄥ皬鍐欙紝涓斿繀椤绘湁搴�
		Map<String, String> params = signParameter.getParams();
		signParameter.initParams(params);
		String content = concatMap2Html(params, encodeCharset, useEncode);
		String signature = _signRSA(digest, content, signKey, signAlgorithmName);
		params.put(SIGNATURE_KEY, signature);
		return params;
	}

	/**
	 * 瀵规寚瀹氬瓧绗︿覆绛惧悕<br>
	 * 
	 * @param digest
	 *            RSA
	 * @param content
	 *            寰呯鍚嶇殑鍐呭
	 * @param signKey
	 *            绛惧悕瀵嗛挜
	 * @param signAlgorithmName
	 *            鏍囧噯绛惧悕绠楁硶鍚嶇О
	 * @return
	 */
	private static String _signRSA(String digest, String content, String signKey, String signAlgorithmName) {
		try {
			java.security.spec.PKCS8EncodedKeySpec privatePKCS8 = new java.security.spec.PKCS8EncodedKeySpec(Base64.decode(signKey));
			java.security.KeyFactory keyf = java.security.KeyFactory.getInstance(digest);
			java.security.PrivateKey priKey = keyf.generatePrivate(privatePKCS8);
			java.security.Signature secSignature = java.security.Signature.getInstance(signAlgorithmName);
			secSignature.initSign(priKey);
			secSignature.update(content.getBytes("UTF-8"));
			byte[] byteDigest = secSignature.sign();
			String signature = Base64.encode(byteDigest);
			logger.debug("\nRSA绛惧悕瀛楃涓诧細" + content + "\n" + "绛惧悕缁撴灉锛�" + signature);
			return signature;
		} catch (Exception e) {
			logger.error("RSA绛惧悕澶辫触銆�", e);
		}
		return null;
	}

	/****************************************** 绛惧悕(E) ******************************************************/

	/**
	 * 绛惧悕鏄瓧绗︿覆锛岄殢鏈烘暟
	 * 
	 * @return
	 */
	public static String createNonceStr() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 绛惧悕鐨勬椂闂存埑锛岀郴缁熷綋鍓嶆椂闂�
	 * 
	 * @return
	 */
	public static String createTimestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	/**
	 * 瀛楄妭鐮佽浆鎹㈡垚16杩涘埗
	 * 
	 * @param hash
	 * @return
	 */
	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	/**
	 * 鎷兼帴Map鎴怘TML鍙傛暟缁撴瀯<br>
	 * key1=val1&key2=val2...(key1锛宬ey2蹇呴』鏄寜鐓SCII鐮佸崌搴忔帓鍒�)
	 * 
	 * @param params
	 *            鍙傛暟Map瀵硅薄
	 * @return 鏈紪鐮佺殑鎷兼帴鍚庨敭鍊煎 锛孧AP涓瓨鏀俱�愭湭缂栫爜銆戝拰銆恥tf8缂栫爜銆戠殑閿�煎
	 * @see #concatMap2Html(Map, String, boolean)
	 */
	public static String concatMap2Html(Map<String, String> params) {
		return concatMap2Html(params, null, false);
	}

	/**
	 * 鎷兼帴Map鎴怘TML鍙傛暟缁撴瀯<br>
	 * key1=val1&key2=val2...(key1锛宬ey2蹇呴』鏄寜鐓SCII鐮佸崌搴忔帓鍒�)
	 * 
	 * @param params
	 *            鍙傛暟Map瀵硅薄
	 * @param encodeCharset
	 *            鍙傛暟鍊糴ncode缂栫爜
	 * @param returnUseEncode
	 *            鍙傛暟杩斿洖鍊煎惁瑕乪ncode缂栫爜
	 * @return returnUseEncode涓簍rue鍒欒繑鍥炵紪鐮佸悗鐨勯敭鍊煎
	 */
	public static String concatMap2Html(Map<String, String> params, String encodeCharset, boolean returnUseEncode) {
		if (params == null) {
			return null;
		}
		if (encodeCharset == null || encodeCharset.length() <= 0) {
			encodeCharset = "UTF-8";
		}
		StringBuilder content = new StringBuilder();
		StringBuilder encodeContent = new StringBuilder();
		Set<String> keySet = params.keySet();
		Iterator<String> iter = keySet.iterator();
		int i = 0;
		while (iter.hasNext()) {
			String key = iter.next();
			String val = params.get(key);
			String encodeVal = val;
			// 绌哄�间笉鍙備笌绛惧悕
			if (val == null || val.length() <= 0) {
				continue;
			}
			// key[API瀵嗛挜]鏀惧埌鏈�鍚�
			if (SIGNATURE_FIELD_KEY.equals(key)) {
				continue;
			}
			if (i != 0) {
				content.append("&");
				encodeContent.append("&");
			}
			try {
				encodeVal = URLEncoder.encode(val, encodeCharset);
			} catch (UnsupportedEncodingException e) {
				// do nothing, keep value
			}
			content.append(key + "=" + val);
			encodeContent.append(key + "=" + encodeVal);
			i++;
		}
		// 鎷兼帴瀹屽悗锛屽鏋滃瓨鍦╧ey锛堟敮浠樼瓑闇�瑕佺殑锛夐偅涔堟嫾鎺ュ埌鏈�鍚�
		String signatureFieldKey = params.get(SIGNATURE_FIELD_KEY);
		if (signatureFieldKey != null && signatureFieldKey.length() > 0) {
			content.append("&" + SIGNATURE_FIELD_KEY + "=" + signatureFieldKey);
			encodeContent.append("&" + SIGNATURE_FIELD_KEY + "=" + signatureFieldKey);
		}
		// 濡傛灉Encode涓嶄负绌猴紝鍒欒繑鍥瀍ncode鍚庣殑瀛楃涓�
		params.put(CONTENT_KEY, content.toString());
		params.put(CONTENT_ENCODE_KEY, encodeContent.toString());
		if (returnUseEncode) {
			return encodeContent.toString();
		} else {
			return content.toString();
		}
	}

	/**
	 * 鎷兼帴Map鎴怷ml鍙傛暟缁撴瀯<br>
	 * <p>
	 * <code>
	 * &lt;xml&gt;<br>
	 * &lt;appid&gt;wx2421b1c4370ec43b&lt;/appid&gt;<br>
	 * ...<br>
	 * &lt;/xml&gt;<br>
	 * </code>
	 * </p>
	 * 
	 * @param params
	 * @return
	 */
	public static String concatMap2Xml(Map<String, String> params) {
		if (params == null) {
			return null;
		}
		StringBuilder ele = new StringBuilder();
		ele.append("<xml>");
		Set<String> keySet = params.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String val = params.get(key);
			// signature鏀惧埌鏈�鍚�
			if (SIGNATURE_KEY.equals(key)) {
				continue;
			}
			ele.append("<" + key + ">");
			ele.append("<![CDATA[" + val + "]]>");
			ele.append("</" + key + ">");
		}

		ele.append("<" + SIGNATURE_KEY + ">");
		ele.append(params.get(SIGNATURE_KEY));
		ele.append("</" + SIGNATURE_KEY + ">");

		ele.append("</xml>");
		return ele.toString();
	}

	/**
	 * 杞崲瀛楃涓茬殑XMl鎴怣AP缁撴瀯<br>
	 * <p>
	 * <code>
	 * &lt;xml&gt;<br>
	 * &lt;appid&gt;wx2421b1c4370ec43b&lt;/appid&gt;<br>
	 * ...<br>
	 * &lt;/xml&gt;<br>
	 * </code>
	 * </p>
	 * 
	 * @param xmlStr
	 * @return
	 */
	public static Map<String, String> concatXml2Map(String xmlStr) {
		Map<String, String> map = new HashMap<String, String>();
		if (xmlStr == null) {
			return map;
		}
		try {
			StringReader sr = new StringReader(xmlStr);
			InputSource is = new InputSource(sr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			Element ele = doc.getDocumentElement();
			NodeList nodes = ele.getChildNodes();
			if (nodes != null) {
				int nodesLen = nodes.getLength();
				for (int i = 0; i < nodesLen; i++) {
					Node node = nodes.item(i);
					String k = node.getNodeName();
					String v = node.getTextContent();
					map.put(k, v);
				}
			}
		} catch (Exception e) {
		}
		return map;
	}
	
	/*******************************鐭繛鎺ュ湴鍧�鐢熸垚(S)*************************************/

	/**
	 * 鐭繛鎺ュ湴鍧�鐢熸垚
	 * 
	 * @param longUrl
	 * @return
	 */
	public static String generateShortUrl(String longUrl) {
		if(longUrl == null || longUrl.length() <= 0) {
			return null;
		}
		//瑕佷娇鐢ㄧ敓鎴怳RL鐨勫瓧绗�   
		String[] chars = new String[]{
            "a","b","c","d","e","f","g","h", "i","j","k","l","m","n","o","p", "q","r","s","t","u","v","w","x", "y","z"
            ,"0","1","2","3","4","5","6","7","8","9"
            ,"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
        };
	
		String shortUrl = longUrl;
		
		// 鐢熸垚MD5
		String signUrl = SignUtils.signMD5(longUrl);
		
		// sign鍒�4绔紝姣忔8浣�
		int segmentCount = signUrl.length() / 8;
		String[] segemnts = new String[segmentCount];
		for (int i = 0; i < segmentCount; i++) {
			// 鑾峰彇绗琲娈�
			String segment = signUrl.substring(i * 8, (i + 1) * 8);
			
			// 姣忔鐨�16杩涘埗瀛楃涓蹭笌0x3fffffff(30浣�1)杩涜涓庢搷浣滐紝瓒呰繃30浣嶇殑蹇界暐澶勭悊
			long idx = Long.valueOf("3FFFFFFF", 16) & Long.valueOf(segment, 16);
			
			// 姣忔寰楀埌鐨勮繖30浣嶅張鍒嗘垚6娈碉紝姣�5浣嶇殑鏁板瓧浣滀负瀛楁瘝琛ㄧ殑绱㈠紩鍙栧緱鐗瑰畾瀛楃锛屼緷娆¤繘琛岃幏寰�6浣嶅瓧绗︿覆
			String genSegment = "";
			for(int j = 0; j < 6; j++) {
				// 00111101
				int index = (int) (Long.valueOf("0000003D", 16) & idx);
				genSegment += chars[index];
				idx = idx >> 5;
			}
			
			segemnts[i] = genSegment;
		}
		if(segemnts.length > 0) {
			java.util.Random random = new java.util.Random();
			return segemnts[random.nextInt(segemnts.length)];
		}
		return shortUrl;
	}
	
	/*******************************鐭繛鎺ュ湴鍧�鐢熸垚(E)*************************************/
	
	/**
	 * 娴嬭瘯
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		kq99bill();
//		wechat();
//		test1();
		test2();
	}

	public static void wechat(){
		
		String timestamp="1501764756";
		String echostr= "14902543494840270230";
		String nonce= "2205663187";
		String token = "b4fc7819b3bc4042b6e5d05d63203f43";
		
		String sign = "ac09c49bedb2995ff0021121afc7cd726aee7189";
		String e = "Agppjk87tGwl7nsXwWd8gXivZoVCuvt0WbtcJMwQA5v";
		
		String[] array = new String[] { timestamp, nonce, token };

		StringBuilder content = new StringBuilder();
		// 瀛楃涓叉帓搴�
		java.util.Arrays.sort(array);
		for (int i = 0; i < array.length; i++) {
			content.append(array[i]);
		}
		System.out.println(content);
//		content.append("nonce=").append(nonce);
//		content.append("&");
//		content.append("timestamp=").append(timestamp);
//		content.append("&");
//		content.append("token=").append(token);

		boolean ret = SignUtils.checkSignSHA1(sign, content.toString());
		System.out.println(ret);
		System.out.println("==============================================");
	}

	public static String getSHA1(String token, String timestamp, String nonce, String encrypt) {
		try {
			String[] array = new String[] { token, timestamp, nonce, encrypt };
			StringBuffer sb = new StringBuffer();
			// 瀛楃涓叉帓搴�
			java.util.Arrays.sort(array);
			for (int i = 0; i < 4; i++) {
				sb.append(array[i]);
			}
			String str = sb.toString();
			// SHA1绛惧悕鐢熸垚
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();

			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int i = 0; i < digest.length; i++) {
				shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * 蹇挶娴嬭瘯
	 * 
	 * @param args
	 */
	public static void kq99bill(){
		String bootPath = "D:\\projects\\鏀粯鍒嗕韩\\蹇挶\\";
		
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDxnaxb/4WhCxMduUd6djmGxG3XteLjdlmJG3KQfM4R4VVyiud9qf4uixA3X/pJNsAcmoXED8k+PJf/yV5Gz/63OYZldHRuXgzOkDv3mC9OLN5ZV2dyUCUGgvk8UaR+ecBZRxlyHQEQg0Tsp/2beE65W3XEffpLyrUo8rsG3kIhhQIDAQAB";
		String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAPGdrFv/haELEx25R3p2OYbEbde14uN2WYkbcpB8zhHhVXKK532p/i6LEDdf+kk2wByahcQPyT48l//JXkbP/rc5hmV0dG5eDM6QO/eYL04s3llXZ3JQJQaC+TxRpH55wFlHGXIdARCDROyn/Zt4TrlbdcR9+kvKtSjyuwbeQiGFAgMBAAECgYBfte72DpV3wvRSnPeUqkqFeUBt5841/sbfCjFqzRWwfYuksSOcYv+enlXRKcLyx654ZjnA/ePhNI2dtL4U3QXv99lbAMnEttsiA8FDDXdQ5H3APE468c/wyUgXDCOb+M6s+5gfUY7+cFAEW7AcMz8p7M7Pkws3d9UO8mAWFSylAQJBAPxBrk6zZQdv/YADGkbGsTeLr2MywnUPaSbfGP+Yp99MrsznzIS+niWqJ8LtmDBx89pl4QrcX8LgaQpBTcyHs0ECQQD1M5GV8bwXZULoue+zlKD7w/+YfpKRgNEls1MTJYtwaIFiVFQi+vHvBPmJEK849Xs40EMLCWLu8lq4feZCZ5FFAkEA24M0cdv+gJhd2rXYedqRgKqXdxVGzqMYd/EQSevHubN7MWhNOv69lD0b2K71DsM61sRtLwxScLzj6z6kuuUjgQJBAJ1qn/qdmN2EOvJh/ItTzg4UBJGIINyc0KjdeWX/Yoel7/qKnQFgDdYfYXwnY/azUopk2cXKHuO8X5W17g+PoekCQQDZ+YQ/Xnhx8i26iqRQkejt+432kApoiKnPWutLO0VjyLvgZ457SnCWYskHactTobV2yyi3p7c4SKX8EjXlkJ8C";
		sun.misc.BASE64Encoder base64Encoder = new sun.misc.BASE64Encoder();
		try {
			// 鑾峰緱鏂囦欢(缁濆璺緞)
			 String platformPubKey = bootPath + "mchPublicKey-rsa.cer";

			java.io.FileInputStream inStream = new java.io.FileInputStream(platformPubKey);

			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
			java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf.generateCertificate(inStream);
			// 鑾峰緱鍏挜
			PublicKey pubKey = cert.getPublicKey();
			String strKey = base64Encoder.encode(pubKey.getEncoded());
			System.out.println(strKey);
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("==============================================");
		try {
			// 鑾峰緱鏂囦欢(缁濆璺緞)
			 String file = bootPath + "mchPrivateKey-rsa.pfx";
			 java.io.FileInputStream ksfis = new java.io.FileInputStream(file);
			java.io.BufferedInputStream ksbufin = new java.io.BufferedInputStream(ksfis);
	
			char[] keyPwd = "123456".toCharArray();
			java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
			ks.load(ksbufin, keyPwd);
			// 浠庡瘑閽ヤ粨搴撳緱鍒扮閽�
			java.security.PrivateKey priK = (java.security.PrivateKey) ks.getKey("test-alias", keyPwd);
			String pk = base64Encoder.encode(priK.getEncoded());
			System.out.println(pk);
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("==============================================");
		String content = "a=12&b=34";
		String sign = signRSA_SHA1(content, privateKey);

		System.out.println(sign);
		System.out.println("==============================================");
		boolean ret = checkSignRSA_SHA1(content, sign, publicKey);
		System.out.println(ret);
	}
	
	/**
	 * 娴嬭瘯
	 * 
	 * @param args
	 */
	public static void test1() {
		// KEY_SIZE=1024
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDRLYybnIOF4/gxvAm7GjLT3TBnZG3XklpcmJ6a7ozgzUD8g4UtcfLSAzNRzTE/adLmlE8r6Axt05uSAvjgzEFQIS3LnKQu80rz2v1xSu5C979V5cN3QyBywHUlstkhFIvWx6tvHCG12H+oXsSKYlVPEj+gZyXM01O6ES2cq7yYzwIDAQAB";
		String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANEtjJucg4Xj+DG8CbsaMtPdMGdkbdeSWlyYnprujODNQPyDhS1x8tIDM1HNMT9p0uaUTyvoDG3Tm5IC+ODMQVAhLcucpC7zSvPa/XFK7kL3v1Xlw3dDIHLAdSWy2SEUi9bHq28cIbXYf6hexIpiVU8SP6BnJczTU7oRLZyrvJjPAgMBAAECgYBpFqYmY4pcELzXV63X1wCkGeLAft8ER12oWXCNS71xDw1QD08wFqg0RDFbLmhIgITZHLdtyoW8A2yL/XxFi7hffcwfD6N1ukn5/rbvyTqbUwu+1zXL1/U4rPwQk94z2/gRlGyO2TmQseM4+FUBnSbotRX08ySM/J3UKaJFd7UgmQJBAOv7y74Bd7w1Luykf/tCdjkL46o0uMbadKPDRkkxNd5MvVc2W9atUKNmcn7UoQT+tgW9JuTP21auRB6/w5s4V0sCQQDi67BkmVv1hMgy9Oz8pNoo8P73B4Y7wv2HAAB1QiCcZ/A//j565AZybChrVPuAxhaUjElnJEwfJISLmRvMtD4NAkBuVp5uKhuFGczmAwiAQBQtGj0KEXqwJ+bPx3dus56/YVxKkfUhDIqezEEOwnbDciFDz6yG4hVpkzPwuLNKOW6/AkAC76nIo3NdKeqtPh4S5s8r2HJ0H9TM0SkWphlbaw+g9ndVXKTSUkEDGgMr4F2gvO9t3KLWgtG5VW+hnsmNTIfBAkEAk8zDdkiLL5sNIP1z7N/Vl0uLTzYkUvAvKZk1LpriXiIHaenzOB54tNVBMFEuSvmhVR7kKrdU79pjWWsZhgS/cw==";

		// 鍟嗘埛绉侀挜
		privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOilN4tR7HpNYvSBra/DzebemoAiGtGeaxa+qebx/O2YAdUFPI+xTKTX2ETyqSzGfbxXpmSax7tXOdoa3uyaFnhKRGRvLdq1kTSTu7q5s6gTryxVH2m62Py8Pw0sKcuuV0CxtxkrxUzGQN+QSxf+TyNAv5rYi/ayvsDgWdB3cRqbAgMBAAECgYEAj02d/jqTcO6UQspSY484GLsL7luTq4Vqr5L4cyKiSvQ0RLQ6DsUG0g+Gz0muPb9ymf5fp17UIyjioN+ma5WquncHGm6ElIuRv2jYbGOnl9q2cMyNsAZCiSWfR++op+6UZbzpoNDiYzeKbNUz6L1fJjzCt52w/RbkDncJd2mVDRkCQQD/Uz3QnrWfCeWmBbsAZVoM57n01k7hyLWmDMYoKh8vnzKjrWScDkaQ6qGTbPVL3x0EBoxgb/smnT6/A5XyB9bvAkEA6UKhP1KLi/ImaLFUgLvEvmbUrpzY2I1+jgdsoj9Bm4a8K+KROsnNAIvRsKNgJPWd64uuQntUFPKkcyfBV1MXFQJBAJGs3Mf6xYVIEE75VgiTyx0x2VdoLvmDmqBzCVxBLCnvmuToOU8QlhJ4zFdhA1OWqOdzFQSw34rYjMRPN24wKuECQEqpYhVzpWkA9BxUjli6QUo0feT6HUqLV7O8WqBAIQ7X/IkLdzLa/vwqxM6GLLMHzylixz9OXGZsGAkn83GxDdUCQA9+pQOitY0WranUHeZFKWAHZszSjtbe6wDAdiKdXCfig0/rOdxAODCbQrQs7PYy1ed8DuVQlHPwRGtokVGHATU=";
		// 鍟嗘埛鍏挜銆愰厤缃湪鏈嶅姟鍣ㄤ笂銆�
		publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDopTeLUex6TWL0ga2vw83m3pqAIhrRnmsWvqnm8fztmAHVBTyPsUyk19hE8qksxn28V6Zkmse7VznaGt7smhZ4SkRkby3atZE0k7u6ubOoE68sVR9putj8vD8NLCnLrldAsbcZK8VMxkDfkEsX/k8jQL+a2Iv2sr7A4FnQd3EamwIDAQAB";

		java.text.SimpleDateFormat dataFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		java.util.Date date = new java.util.Date();
		final String timeString = dataFormat.format(date);
		final String OID_PARTNER = "201408071000001546";
		final String MD5_KEY = "201408071000001546_test_20140815";
		final String NOTIFY_URL = "http://test.yintong.com.cn:80/apidemo/API_DEMO/notifyUrl.htm";
		final String SIGN_TYPE = "RSA";// "MD5";
		final String BUSI_PARTNER = "101001";
		final String VALID_ORDER = "10080";
		final String riskItem = "{\"frms_ware_category\":\"1999\",\"user_info_full_name\":\"浣犲ソ\"}";

//		**************************************
		try {
			String signMsg = "a=12&b=34";
			String file = "D:\\DevWorkSpace\\PlatformDemo\\KQ99BILL\\src\\Util\\tester-rsa.pfx";
			java.io.FileInputStream ksfis = new java.io.FileInputStream(file);
			java.io.BufferedInputStream ksbufin = new java.io.BufferedInputStream(ksfis);
	
			char[] keyPwd = "123456".toCharArray();
			//char[] keyPwd = "YaoJiaNiLOVE999Year".toCharArray();
			java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
			ks.load(ksbufin, keyPwd);
			// 浠庡瘑閽ヤ粨搴撳緱鍒扮閽�
			java.security.PrivateKey priK = (java.security.PrivateKey) ks.getKey("test-alias", keyPwd);
			
			sun.misc.BASE64Encoder base64Encoder = new sun.misc.BASE64Encoder();
			String pk = base64Encoder.encode(priK.getEncoded());
			System.out.println(pk);
			
			java.security.Signature signature = java.security.Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			signature.update(signMsg.getBytes("utf-8"));
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			String base64 = encoder.encode(signature.sign());
			System.out.println(base64);
			System.out.println("**************************************");
			// 鑾峰緱绛惧悕
			String signContent = "inputCharset=1&bgUrl=http://219.233.173.50:8801/RMBPORT/receive.jsp&version=mobile1.0&language=1&signType=4&merchantAcctId=1001213884201&payerContactType=1&payerContact=2532987@qq.com&payerIdType=3&payerId=3315&orderId=20170801132715&orderAmount=1&orderTime=20170801132715&productName=鑻规灉&productNum=5&productId=55558888&payType=21&mobileGateway=phone";
			String rsa = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKv+9u3j0kFVVZRtglikVTDX8sz6OVJDw0hSZHedO5+fMAPHgn7LEq0qk4MzWtPVfauba256JXuA2DpomtOFrRqLuxJEvW1gzOSTLgVHAttRfYhJ7KF4S/0/fn72SL6HINHv7xqMSqQC4AtfrijgbjRwfS3W1NZlQgpoE/AN6aEFAgMBAAECgYEAhKv628vlrsHdbs3+QtQYZsHdJ6JIvx1IMKxllSLzEBDiH7gxAA8zS+JQwV7GzCqdctdDSofsC9V6dauk6k7uFA1FGSGVB8kapYFSw13RUj50dVGi3F/eA4lN/RpEA4Xm1+TsQeWC/WmnW9PQbi+Rxmozc6mN0ojCm6Rc74zo/A0CQQDSYXBin2QYT2ucqY7JevAx5sDrJSyVQwGO8Y8Hqe2o9B9MESm17RPk37TJUuC5sffWkgj1QWCYC6fgHb+gTV+TAkEA0Uq7aEwPn2LT6Ef8M769rNm9uQyHhhLLjgbSBdvDGWvkdK4Oe69lv6KOQ9pKtWiN6KjCyVo8kgtF6NkeAt5sBwJAZoSU/3osjKwnBHCb5BLEeYy49d1nnFTKrZ2I1XM5HNvZZHf4m26sAxwAPRrl55eR7j27n8f8Chuj8tKMTtFlgQJATR9yy84re5pZaCEOqKrDDmz2YrhhQGCwrdeJkSsYS8fcWbrCD4XkwqdOMWbBXPP4RyHZFYWxCEgrLNDFJF1+BQJAPJ65F0yWOisn9VC2jxnyKc8TuITuDSv7dSCy3QInhn+MR2/02a3BDx284WvXPC9ZjV2pfROMs+KepVOP0nvxEQ==";
			System.out.println(signRSA_SHA1(signContent,rsa));
			

			System.out.println("**************************************");
			try {
				// 鑾峰緱鏂囦欢(缁濆璺緞)
				 String platformPubKey = "D:\\DevWorkSpace\\PlatformDemo\\KQ99BILL\\src\\Util\\99bill[1].cert.rsa.20140803.cer";
				 platformPubKey = "D:\\projects\\鏀粯鍒嗕韩\\蹇挶\\99bill.cert.rsa.20340630.cer";

				java.io.FileInputStream inStream = new java.io.FileInputStream(platformPubKey);

				java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
				java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf.generateCertificate(inStream);
				// 鑾峰緱鍏挜
				PublicKey pubKey = cert.getPublicKey();
				String strKey = base64Encoder.encode(pubKey.getEncoded());
				System.out.println("瑙ｆ瀽鍚庡瘑閽ワ細\n" + strKey);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("no");
			}
			
		}catch(Exception e) {}

	}

	/**
	 * 娴嬭瘯
	 * 
	 * @param args
	 */
	public static void test2() {
//		String[] args = new String[]{"1212","4654"};
//		String str = JSON.toJSONString(args);
//		System.out.println(str);
//		
//		str = "{\"code\":200,\"uinfos\":[{\"email\":\"t1@163.com\",\"accid\":\"t1\",\"name\":\"abc\",\"gender\":1,\"mobile\":\"18645454545\"},{\"accid\":\"t2\",\"name\":\"def\",\"gender\":0}]}";
//		JSONObject jo = JSON.parseObject(str);
//		if(jo.getIntValue("code") == 200) {
//			JSONArray ja = jo.getJSONArray("uinfos");
//			List<Object> l = ja.subList(0, ja.size());
//			for(int i = 0; i < l.size(); i++) {
//				Map<String, Object> m = (Map)l.get(0);
//				int cc = 0;
//			}
//		}
		
		String fullUrl = "https://m.heyoucloud.com/html/house/other/fy-download.html?u=99477&shareId=99477&track_id=%257B%2522s_module%2522%253A%2522app%2522%252C%2522s_title%2522%253A%2522%25E4%25B8%258B%25E8%25BD%25BD%25E6%2588%25BF%25E9%2593%25B6APP%25EF%25BC%258C%25E6%2590%25AD%25E4%25B8%258A%25E6%2588%2590%25E5%258A%259F%25E7%259A%2584%25E9%25A1%25BA%25E9%25A3%258E%25E8%25BD%25A6%2522%252C%2522s_res_key%2522%253A%2522%2522%252C%2522s_res_val%2522%253A%2522%2522%252C%2522s_id%2522%253A99477%252C%2522s_time%2522%253A%25222017-08-31%252018%253A19%253A19%2522%257D";
		System.out.println(generateShortUrl(fullUrl));
	}

	/**
	 * 鐢熸垚sign
	 */
	public static void test3() {
		String nonce= "";
		String timestamp = "";
		String[] array = new String[]{nonce,timestamp,CACHEKEY_TOKEN};

		StringBuilder content = new StringBuilder();
		java.util.Arrays.sort(array);
		for (int i = 0; i < array.length; i++) {
			content.append(array[i]);
		}
		// SHA1绛惧悕楠岃瘉
		String sign  = signSHA1(content.toString());
		System.out.println("绛惧悕sign: "+ sign);

	}
}
