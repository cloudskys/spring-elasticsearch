package com.fxwiz.base.security;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名参数
 * 
 * @author xuyd
 * 
 */
public abstract class SignParameter {

	protected Map<String, String> params = new TreeMap<String, String>(new Comparator<String>() {
		public int compare(String args1, String args2) {
			// 升序排列
			return args1.compareTo(args2);
		}
	});

	public Map<String, String> getParams() {
		return params;
	}

	public abstract void initParams(Map<String, String> map);
}
