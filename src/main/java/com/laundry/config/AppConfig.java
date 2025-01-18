package com.laundry.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@Getter
public class AppConfig {

    @Value("${app.base.url}")
    private String baseUrl;
}
