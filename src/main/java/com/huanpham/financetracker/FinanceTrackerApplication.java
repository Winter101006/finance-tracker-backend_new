package com.huanpham.financetracker;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
public class FinanceTrackerApplication {

	public static final String TIMEZONE_ID = "Asia/Ho_Chi_Minh";
	public static final ZoneId VIETNAM_ZONE = ZoneId.of(TIMEZONE_ID);

	@PostConstruct
	public void init() {
		// Đồng bộ múi giờ mặc định sang GMT+7 (Asia/Ho_Chi_Minh) cho toàn bộ JVM / Spring Context
		TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE_ID));
	}

	public static void main(String[] args) {
		// Thiết lập múi giờ sớm ngay từ khi chạy hàm main
		TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE_ID));

		// Tránh lỗi UnknownHostException khi tên máy tính chứa ký tự Unicode (tiếng Việt) khi dùng H2 AUTO_SERVER
		if (System.getProperty("h2.bindAddress") == null) {
			System.setProperty("h2.bindAddress", "127.0.0.1");
		}
		SpringApplication.run(FinanceTrackerApplication.class, args);
	}

}
