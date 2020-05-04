package com.fenghm.ethdapp.mytoken.common;

import java.util.Random;

public class Tool {

	// 产生以“0x”为前缀的随机16进制字符串
	public static String randomHexString(int len) {
		StringBuffer result = new StringBuffer("0x");
		for (int i = 0; i < len; i++) {
			result.append(Integer.toHexString(new Random().nextInt(16)));
		}
		return result.toString().toLowerCase();
	}

}
