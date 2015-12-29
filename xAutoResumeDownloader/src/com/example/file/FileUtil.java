package com.example.file;

public class FileUtil {
	/**
	 * 获取网络文件的名称，包含格式
	 * @param url
	 * @return
	 */
	public static String getFileName(String url){
		if (url == null) {
			return null;
		}
		String[] strs = url.split("/");
		return strs[strs.length - 1];
	}
}
