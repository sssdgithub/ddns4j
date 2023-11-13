package top.sssd.ddns.factory;

import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.service.impl.AliDynamicDnsServiceImpl;
import top.sssd.ddns.service.impl.CloudflareDynamicDnsServiceImpl;
import top.sssd.ddns.service.impl.HuaweiDynamicDnsServiceImpl;
import top.sssd.ddns.service.impl.TencentDynamicDnsServiceImpl;

/**
 * @author sssd
 * @created 2023-05-06-17:07
 */
public class DynamicDnsServiceFactory {

    private DynamicDnsServiceFactory(){}

    public static DynamicDnsService getServiceInstance(Integer serviceProvider) {
        DynamicDnsService service;
        switch (serviceProvider) {
            case 1:
                service = new AliDynamicDnsServiceImpl();
                break;
            case 2:
                service = new TencentDynamicDnsServiceImpl();
                break;
            case 3:
                service = new CloudflareDynamicDnsServiceImpl();
                break;
            case 4:
                service = new HuaweiDynamicDnsServiceImpl();
                break;
            default:
                throw new IllegalArgumentException("Unsupported service provider: " + serviceProvider);
        }
        return service;
    }

}
