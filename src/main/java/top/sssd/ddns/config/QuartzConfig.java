package top.sssd.ddns.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * @author sssd
 * @created 2023-05-02-11:07
 */
public class QuartzConfig implements SchedulerFactoryBeanCustomizer {
    @Autowired
    private JobFactory jobFactory;

    @Override
    public void customize(SchedulerFactoryBean schedulerFactoryBean) {
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setStartupDelay(10);
    }

    @Bean
    public JobFactory jobFactory() {
        return new SpringBeanJobFactory();
    }
}
