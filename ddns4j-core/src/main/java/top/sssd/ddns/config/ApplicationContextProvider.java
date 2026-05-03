package top.sssd.ddns.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author sssd
 * @created 2023-12-26-0:39
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
       setContext(applicationContext);
    }

    public static synchronized ApplicationContext getContext() {
        return context;
    }

    public static synchronized void setContext(ApplicationContext context){
        ApplicationContextProvider.context = context;
    }


}
