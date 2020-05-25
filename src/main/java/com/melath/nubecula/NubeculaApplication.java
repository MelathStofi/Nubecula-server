package com.melath.nubecula;

import com.melath.nubecula.storage.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class NubeculaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NubeculaApplication.class, args);
    }

    /*@Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }*/

}
