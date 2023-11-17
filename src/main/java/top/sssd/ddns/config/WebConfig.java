package top.sssd.ddns.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.sssd.ddns.interceptor.ExcludeIndexPageInterceptor;

/**
 * @author sssd
 * @careate 2023-11-17-0:07
 */
@Configuration
public class WebConfig  implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ExcludeIndexPageInterceptor());
    }
}
