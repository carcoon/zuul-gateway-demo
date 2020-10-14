/**
 * eureka server
 * @author carcoon@163.com
 *
 */
package com.hhd.eureka;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

//@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
@EnableEurekaServer
public class EurekaServer {
	public static void main(String[] args) {
        SpringApplication.run(EurekaServer.class, args);
  }
}
