package com.train.platform.daemon.quartz;

import com.train.platform.common.feign.annotation.EnablePlatformFeignClients;
import com.train.platform.common.security.annotation.EnablePlatformResourceServer;
import com.train.platform.common.swagger.annotation.EnablePlatformDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author frwcloud
 * @date 2024-07-05
 */
@EnablePlatformDoc("job")
@EnablePlatformFeignClients
@EnablePlatformResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class PlatformQuartzApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatformQuartzApplication.class, args);
	}

}
