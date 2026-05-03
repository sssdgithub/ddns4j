package top.sssd.ddns.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author sssd
 * @created 2024-01-08-19:14
 */
@Configuration
public class IdentifierGeneratorConfig {


    @Bean
    public DefaultIdentifierGenerator build(){
        return new DefaultIdentifierGenerator();
    }
}
