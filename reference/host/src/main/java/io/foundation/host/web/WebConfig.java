package io.foundation.host.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PageAuthInterceptor())
                .addPathPatterns("/", "/endpoints", "/endpoints/**", "/modules", "/modules/**", "/audit");
    }
}
