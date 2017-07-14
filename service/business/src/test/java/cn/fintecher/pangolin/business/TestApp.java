package cn.fintecher.pangolin.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"cn.fintecher.pangolin.business.repository", "cn.fintecher.pangolin.entity"})
@EntityScan("cn.fintecher.pangolin.entity")
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
