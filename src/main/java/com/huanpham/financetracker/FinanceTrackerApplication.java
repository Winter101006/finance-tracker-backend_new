package com.huanpham.financetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanceTrackerApplication {

	public static void main(String[] args) {
		// Tránh lỗi UnknownHostException khi tên máy tính chứa ký tự Unicode (tiếng Việt) khi dùng H2 AUTO_SERVER
		if (System.getProperty("h2.bindAddress") == null) {
			System.setProperty("h2.bindAddress", "127.0.0.1");
		}
		SpringApplication.run(FinanceTrackerApplication.class, args);
	}

}
