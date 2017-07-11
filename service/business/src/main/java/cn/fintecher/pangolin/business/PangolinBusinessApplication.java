package cn.fintecher.pangolin.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EntityScan("cn.fintecher.pangolin.entity.*")
public class PangolinBusinessApplication {

	public static void main(String[] args) {
		SpringApplication.run(PangolinBusinessApplication.class, args);
	}
}
