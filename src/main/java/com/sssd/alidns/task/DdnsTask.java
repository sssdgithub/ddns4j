package com.sssd.alidns.task;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.sssd.alidns.service.AliDnsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author sssd
 * @update 2022-12-18 18:36
 */
@Component
@Slf4j
public class DdnsTask {

    @Autowired
    private AliDnsService aliDnsService;


    @Scheduled(cron = "${cron}")
    public void ddns() throws Exception {
        log.info("ddns start...");
        Properties sysProperties = System.getProperties();
        String osName = sysProperties.getProperty("os.name");
        String realIpv6 = null;
        //获取可用的ipv6
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
//                log.info("inetAddress:{}", inetAddress.getHostAddress());
                if (StrUtil.containsIgnoreCase(osName,"windows")) {
                    if (inetAddress instanceof Inet6Address && !inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()) {
                        // FIXME: 2022/12/18 linux系统会在显示的ip地址后方带上%指定网卡名,window系统会直接显示
                        String[] v6Split = inetAddress.getHostAddress().split(":");
                        String lastValue = v6Split[v6Split.length - 1];
                        if(NumberUtil.isNumber(lastValue)){
                            realIpv6 = inetAddress.getHostAddress();
                            log.info("realIpv6:{}", realIpv6);
                        }
                    }
                }else{
                    // FIXME: 2022/12/18 linux系统会在显示的ip地址后方带上%指定网卡名,window系统会直接显示
                    if (inetAddress instanceof Inet6Address && !inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()) {
                        String[] networkSplit = inetAddress.getHostAddress().split("%");
                        String ipv6 = networkSplit[0];
                        String[] v6Split = ipv6.split(":");
                        String lastValue = v6Split[v6Split.length - 1];
                        if(NumberUtil.isNumber(lastValue)){
                            realIpv6 = ipv6;
                            log.info("realIpv6:{}", realIpv6);
                        }
                    }
                }
            }
        }
        //执行解析
        String updateResult = aliDnsService.update(realIpv6);
        log.info("updateResult:{}", updateResult);
        log.info("ddns end...");
    }
}
