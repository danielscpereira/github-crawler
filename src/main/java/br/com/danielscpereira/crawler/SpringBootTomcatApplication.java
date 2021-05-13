package br.com.danielscpereira.crawler;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication

@EnableEncryptableProperties
public class SpringBootTomcatApplication extends SpringBootServletInitializer {

    static {
        System.setProperty("jasypt.encryptor.password", "!Sesp2019");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringBootTomcatApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTomcatApplication.class, args);
    }

}
