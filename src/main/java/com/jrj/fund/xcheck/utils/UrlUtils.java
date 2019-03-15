package com.jrj.fund.xcheck.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

public class UrlUtils{
	public static String getHost(String url) {
		try {
			return new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getHostAndPath(String url) {
		if (StringUtils.isBlank(url)) {
			return "";
		}
		try {
			URL u = new URL(url);
			return u.getHost() + u.getPath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return "";
	}
}
