package org.august.lock.spring.boot.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 工具类
 * @author TanRq
 *
 */
public class LockUtil {
	
	/**
	 * 生成随机key
	 * @return
	 */
	public static String generateRandomKey() {
		String dateKey = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
		Random random=new Random();
		int randomKey = random.ints(100000, 999999).limit(1).findFirst().getAsInt();
		return new StringBuilder(dateKey).append(randomKey).toString();
	}

}
