package com.mb.config;

import com.mb.properties.ExportProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 石鹏
 * @date 2024/12/20 16:52
 */
@Configuration
@EnableConfigurationProperties(ExportProperties.class)
public class ExportPropertiesAutoConfig {

    @Bean
    public ExportProperties exportProperties() {
        return new ExportProperties();
    }
}
