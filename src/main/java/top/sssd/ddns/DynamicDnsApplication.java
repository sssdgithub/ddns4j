package top.sssd.ddns;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author sssd
 * @created 2023-03-19-1:24
 */
@MapperScan("top.sssd.ddns.mapper")
@SpringBootApplication
public class DynamicDnsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicDnsApplication.class);
    }
}
