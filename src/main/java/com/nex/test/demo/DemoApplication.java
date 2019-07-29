package com.nex.test.demo;

import com.nex.test.demo.adapter.nef.impl.NefSourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    NefSourceServiceImpl nefSourceService;

    public static void main(String[] args) throws IOException {

        SpringApplication.run(DemoApplication.class, args);

    }

    @Override
    public void run(String... args) throws IOException {

        System.out.println(nefSourceService.healthCheck());
        //System.in.read();
        //JUnitCore junit = new JUnitCore();
        //Result result = junit.run(CucumberTest.class);

        //System.exit(0);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
