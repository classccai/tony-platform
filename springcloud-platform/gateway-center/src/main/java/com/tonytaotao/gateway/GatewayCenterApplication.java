package com.tonytaotao.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class GatewayCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayCenterApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
