package com.trabalho.api;

import com.trabalho.api.logging.AppLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProdutoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdutoApiApplication.class, args);
        AppLogger.getInstance().info("ProdutoApiApplication", "API de Produtos iniciada");
    }
}
