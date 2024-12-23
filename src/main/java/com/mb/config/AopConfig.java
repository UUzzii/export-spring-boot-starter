package com.mb.config;

import com.mb.aspect.ExportAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AOP配置
 * @author cxn
 * @date 2024/12/18 11:41
 */
@Configuration
public class AopConfig {

    @Bean
    public ExportAspect exportAspect() {
        return new ExportAspect();
    }
}
