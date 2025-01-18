package com.laundry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ApiConfig {

    private final Environment env;

    public ApiConfig(Environment env) {
        this.env = env;
    }

    public String getApiVersion() {
        return env.getProperty("api.base.path");
    }
}
