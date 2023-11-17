package top.sssd.ddns.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext APPLICATION_CONTEXT;


    @Override
    public  void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.APPLICATION_CONTEXT = applicationContext;
    }

    public static <T> T getBean(Class<T> tClass) {
        return APPLICATION_CONTEXT.getBean(tClass);
    }

    public static ApplicationContext getApplicationContext(){
        return APPLICATION_CONTEXT;
    }
}
