package ru.itmo.ipkn.yuriufimtsev.service.member.config;

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
                        .title("Member Service API")
                        .description("Сервис управления посетителями, абонементами и посещениями")
                        .version("1.0"));
    }
}
