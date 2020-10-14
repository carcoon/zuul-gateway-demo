package com.hhd.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.ZuulProxyAutoConfiguration;
import org.springframework.cloud.netflix.zuul.ZuulServerAutoConfiguration;

@EnableZuulProxy
@SpringBootApplication(exclude = {ZuulProxyAutoConfiguration.class, ZuulServerAutoConfiguration.class})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}