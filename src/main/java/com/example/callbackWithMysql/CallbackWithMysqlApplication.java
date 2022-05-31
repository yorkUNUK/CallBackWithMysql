package com.example.callbackWithMysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication()
public class CallbackWithMysqlApplication {

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(CallbackWithMysqlApplication.class);
		SpringApplication.run(CallbackWithMysqlApplication.class, args);
		logger.info("----------------启动成功----------------");
	}
}
