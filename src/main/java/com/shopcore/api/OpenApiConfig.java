package com.shopcore.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shopCoreOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ShopCore API")
                .version("v1")
                .description("Products, checkout and order history for the ShopCore learning project. "
                        + "Simulated data only."));
    }
}