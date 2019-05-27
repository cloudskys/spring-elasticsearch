package com.fxwiz.base.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 扩展框架的PropertyPlaceholderConfigurer
 * 
 * @author xuyd
 */
public class PropertyConfigurerEx extends PropertyPlaceholderConfigurer {

	/**
	 * properties文件内容
	 */
	private static Map<String, Object> ctxPropertiesMap = null;

	/**
	 * Pro文件�?要的解密密钥路径
	 */
	private String proKeyPath = null;
	
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {

		ctxPropertiesMap = new HashMap<String, Object>();
		String k = null;
		
		for (Object key : props.keySet()) {
			k = key.toString();
			if("proKeyPath".equals(k)) {
				proKeyPath = props.getProperty(k);
				break;
			}
		}
		PropertiesExpressionParser expParser = PropertiesExpressionParser.getInstance();
		expParser.setProKeyPath(proKeyPath);
		// 解析配置文件
		for (Object key : props.keySet()) {
			k = key.toString();
			ctxPropertiesMap.put(k, expParser.parse(k, props.getProperty(k)));
		}
		super.processProperties(beanFactoryToProcess, props);
	}

	/**
	 * Xml中只有使用的才会被执行此方法�?<br>
	 * �?以config.properties文件就没有执行此方法�?<br>
	 * 但是props中已经加载所有定义了<br>
	 */
	@Override
	protected String resolvePlaceholder(String key, Properties props) {
		String value = props.getProperty(key);
		PropertiesExpressionParser expParser = PropertiesExpressionParser.getInstance();
		expParser.setProKeyPath(proKeyPath);
		value = String.valueOf(expParser.parse(key, value));
		return value;
	}

	/**
	 * 获取properties文件指定�?
	 * 
	 * @param name
	 * @return
	 */
	public static String getProperty(String name) {
		return (String)ctxPropertiesMap.get(name);
	}
	
	/**
	 * 获取properties文件指定�?
	 * 
	 * @param name
	 * @return
	 */
	public static Object get(String name) {
		return ctxPropertiesMap.get(name);
	}
}
