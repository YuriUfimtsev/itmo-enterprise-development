package ru.itmo.ipkn.yuriufimtsev.service.rateplan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RatePlan Service API")
                        .description("Сервис управления тарифными планами")
                        .version("1.0"));
    }
}
