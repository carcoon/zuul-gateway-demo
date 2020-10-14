package com.hhd.vehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class VehicleApplication {

	@RequestMapping(value = "/available")
	public String available() {
		System.out.println("Spring in vehilce");
		return "Spring in  vehilce";
	}

	@RequestMapping(value = "/checked-out")
	public String checkedOut() {
		return "Spring Boot in vehilce";
	}

	public static void main(String[] args) {
		SpringApplication.run(VehicleApplication.class, args);
	}
}