package com.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CustomizedPropertyConfigurer extends PropertyPlaceholderConfigurer {

	private static Map<String, String> ctxPropMap;

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		ctxPropMap = new HashMap<String, String>();
		for (Object key : props.keySet()) {
			String keyStr = key.toString();
			String value = String.valueOf(props.get(keyStr));
			ctxPropMap.put(keyStr, value);
		}
	}

	public static String getCtxProp(String name) {
		return ctxPropMap.get(name);
	}

	public static Map<String, String> getCtxPropMap() {
		return ctxPropMap;
	}
}