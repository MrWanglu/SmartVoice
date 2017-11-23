package cn.fintecher.pangolin.gateway;


import cn.fintecher.pangolin.gateway.filter.AccessFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.MultipartConfigElement;

@EnableZuulProxy
@SpringCloudApplication
public class PangolinZuulServerApplication {

    @Autowired
    private MultipartProperties multipartProperties;

    public static void main(String[] args) {
        new SpringApplicationBuilder(PangolinZuulServerApplication.class).web(true).run(args);
    }

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addExposedHeader("X-pangolin-error");
        corsConfig.addExposedHeader("X-pangolin-alert");
        corsConfig.addExposedHeader("X-pangolin-params");
        corsConfig.addAllowedMethod("OPTIONS");
        corsConfig.addAllowedMethod("HEAD");
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("PUT");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("DELETE");
        corsConfig.addAllowedMethod("PATCH");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public MultipartConfigElement multipartConfigElement() {
        this.multipartProperties.setMaxFileSize("1024MB");
        this.multipartProperties.setMaxRequestSize("1024MB");
        return this.multipartProperties.createMultipartConfig();
    }

    @Bean
    public AccessFilter accessFilter() {
        return new AccessFilter();
    }
}
