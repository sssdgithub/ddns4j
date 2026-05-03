package top.sssd.ddns.strategy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.sssd.ddns.config.DnsServiceTypeConfig;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sssd
 * @created 2023-05-06-17:07
 */
@Component
public class DynamicDnsServiceFactory implements ApplicationContextAware {

    private static Map<Integer,DynamicDnsStrategy> dnsStrategyMap = new ConcurrentHashMap<>();

    @Resource
    private DnsServiceTypeConfig dnsServiceTypeConfig;

    private DynamicDnsServiceFactory(){}

    public DynamicDnsStrategy getServiceInstance(Integer serviceProvider) {
        return dnsStrategyMap.get(serviceProvider);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        dnsServiceTypeConfig.getServiceTypes().forEach((k,v)->dnsStrategyMap.put(k,(DynamicDnsStrategy)applicationContext.getBean(v)));
    }


}
