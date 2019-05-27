package com.fxwiz.base.framework;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fxwiz.base.security.AlgorithmDES;

/**
 * 自定义表达式解析 没有缓存，初始化每次都读取一次
 * 
 * @author xuyd
 * 
 */
public class PropertiesExpressionParser {

	protected static final Logger logger = LoggerFactory.getLogger(PropertiesExpressionParser.class);

	private static PropertiesExpressionParser _self;
	
	/**
	 * 配置文件的密钥路径
	 */
	private String proKeyPath;
	
	/**
	 * 设置密钥路径
	 * @param proKeyPath
	 */
	public void setProKeyPath(String proKeyPath) {
		this.proKeyPath = proKeyPath;
	}

	private PropertiesExpressionParser(){}
	
	/**
	 * 单例获取对象
	 * 
	 * @return
	 */
	public static PropertiesExpressionParser getInstance() {
		if(_self == null) {
			_self = new PropertiesExpressionParser();
		}
		return _self;
	}
	
	/**
	 * 解析
	 * 
	 * @param express
	 */
	public Object parse(String key, String express) {
		if (key == null || key.length() <= 0) {
			return null;
		}
		if (express == null || express.length() <= 0) {
			return null;
		}
		// 表达式解析
		String val = express;
		if (express.startsWith("$")) {
			Object parsedVal = null;
			while (val.indexOf("$") != -1) {
				int pos = val.indexOf("(");
				if (pos < 0) {
					throw new IllegalArgumentException("invalid express." + express);
				}
				// 获得方法名
				String methodName = val.substring(1, pos);
				val = val.substring(pos + 1);

				// 获得参数
				pos = val.indexOf(")");
				if (pos < 0) {
					throw new IllegalArgumentException("invalid express." + express);
				}
				String param = val.substring(0, pos);
				val = val.substring(pos + 1);
				if (val.length() > 0 && '.' == val.charAt(0)) {
					val = val.substring(1);
				}
				if (param == null || param.length() <= 0) {
					parsedVal = exec(methodName, new Object[] { key, parsedVal }, new Class[] { String.class,
							Object.class });
				} else {
					parsedVal = exec(methodName, new Object[] { key, parsedVal, param }, new Class[] { String.class,
							Object.class, String.class });
				}
			}
			return parsedVal;
		}
		return val;
	}

	/**
	 * 解密
	 * 
	 * @param key
	 *            需要获取的KEY
	 * @param result
	 *            前一个方法返回值
	 */
	public String decrypt(String key, Object result) {
		if (key == null || key.length() <= 0) {
			return null;
		}
		if (result == null) {
			return null;
		}
		String ciphertext = (String) result;
		AlgorithmDES algorithmDES = AlgorithmDES.getInstance();
		String val = algorithmDES.setDesKeyPath(proKeyPath).decrypt(ciphertext);
		return val;
	}

	/**
	 * 解密
	 * 
	 * @param key
	 *            需要获取的KEY，properties中配置的键名称
	 * @param result
	 *            前一个方法返回值， null
	 * @param ciphertext
	 *            密文
	 */
	public String decrypt(String key, Object result, String ciphertext) {
		if (ciphertext == null || ciphertext.length() <= 0) {
			return null;
		}

		AlgorithmDES algorithmDES = AlgorithmDES.getInstance();
		String val = algorithmDES.setDesKeyPath(proKeyPath).decrypt(ciphertext);
		return val;
	}

	/**
	 * 加载文件，得到Properties对象
	 * 
	 * @param key
	 *            需要获取的KEY
	 * @param result
	 *            前一个方法返回值
	 * @param filePath
	 * @return
	 */
	public static Properties path(String key, Object result, String filePath) {
		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			Properties pro = new Properties();
			pro.load(in);
			return pro;
		} catch (Exception e) {
			throw new IllegalArgumentException("load file failed.invalid file path:" + filePath);
		}
	}

	/**
	 * 从properties中获取键keyName的值
	 * 
	 * @param key
	 *            需要获取的KEY
	 * @param result
	 *            前一个方法返回值
	 * @return
	 */
	public Object get(String key, Object result) {
		Properties pro = (Properties) result;
		if (pro == null) {
			return null;
		}
		return pro.get(key);
	}

	/**
	 * 静态方法执行
	 * 
	 * @param methodName
	 *            方法名
	 * @param args
	 *            参数，对象数组
	 * @param parameterTypes
	 *            参数类型，对象数组
	 * @return
	 */
	public Object exec(String methodName, Object[] args, Class<?>[] parameterTypes) {
		try {
			// 静态类
//			Method method = PropertiesExpressionParser.class.getMethod(methodName, parameterTypes);
//			Object obj = method.invoke(null, args);
			// 成员方法
			Method method = this.getClass().getMethod(methodName, parameterTypes);
			Object obj = method.invoke(this, args);
			return obj;
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	/**
	 * Test
	 * 
	 * @param args
	 */
	public static void main1(String[] args) {
		
		// 文件中直接解密
		// jdbc.password=$decrypt(mrLyVojEDEA=);
		// 引用其他公共文件直接获取值
		// jdbc.username=$path(d:/jdbc.properties).$get();
		// 引用其他公共文件，获取值后解密
		// jdbc.username=$path(d:/jdbc.properties).$get().$decrypt();
		
		// 文件中直接解密
		String str1 = "$decrypt(mrLyVojEDEA=)";
		// 引用其他公共文件直接获取值
		String str2 = "$path(d:/jdbc.properties).$get()";
		// 引用其他公共文件，获取值后解密
		String str3 = "$path(d:/jdbc.properties).$get().$decrypt()";
		PropertiesExpressionParser expParser = PropertiesExpressionParser.getInstance();
		String proKeyPath = "C:/home/hefa/sec/deskey.lic";
		expParser.setProKeyPath(proKeyPath);
		logger.info(expParser.parse("jdbc.username", str1).toString());
		logger.info(expParser.parse("jdbc.username", str2).toString());
		logger.info(expParser.parse("jdbc.username", str3).toString());
	}
}
