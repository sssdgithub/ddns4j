package top.sssd.ddns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author sssd
 * @created 2024-05-08-10:58
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "dns")
public class DnsServiceTypeConfig {

    private Map<Integer,String> serviceTypes;
}
