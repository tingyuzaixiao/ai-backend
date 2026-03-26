package com.train.platform;

import com.train.platform.common.security.annotation.EnablePlatformResourceServer;
import com.train.platform.common.swagger.annotation.EnablePlatformDoc;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lee
 */
@EnablePlatformDoc(value = "admin", isMicro = false)
@EnablePlatformResourceServer
@SpringBootApplication
@EnableFileStorage
public class PlatformBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatformBootApplication.class, args);
	}

}
