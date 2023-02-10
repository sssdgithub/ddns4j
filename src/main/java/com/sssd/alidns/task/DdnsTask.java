package com.sssd.alidns.task;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.tea.utils.StringUtils;
import com.sssd.alidns.service.AliDnsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

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
        String realIpv6 = null;

        //获取可用的ipv6
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                String hostAddress = inetAddress.getHostAddress();
                if (StringUtils.isEmpty(hostAddress)) {
                    continue;
                }

                if (isWin(inetAddress)) {
                    // FIXME: 2022/12/18 linux系统会在显示的ip地址后方带上%指定网卡名,window系统会直接显示
                    // FIXME: 2023/2/10 window系统会在显示的ipv6地址中 有 :0: 导致解析失败
                    String winRealIpv6 = getWinRealIpv6(hostAddress);
                    if (StringUtils.isEmpty(winRealIpv6)) {
                        continue;
                    }
                    realIpv6 = winRealIpv6;
                    log.info("realIpv6:{}", realIpv6);
                } else if (isLinux(inetAddress)) {
                    // FIXME: 2022/12/18 linux系统会在显示的ip地址后方带上%指定网卡名,window系统会直接显示
                    // FIXME: 2023/2/10 linux系统会在显示的ipv6地址中 有 :0: 导致解析失败
                    String linuxRealIpv6 = getLinuxRealIpv6(hostAddress);
                    if (StringUtils.isEmpty(linuxRealIpv6)) {
                        continue;
                    }

                    realIpv6 = linuxRealIpv6;
                    log.info("realIpv6:{}", realIpv6);
                }
            }
        }
        //执行解析
        if (StringUtils.isEmpty(realIpv6)) {
            log.warn("没有可解析的ipv6地址");
            return;
        }
        String updateResult = aliDnsService.update(realIpv6);
        log.info("updateResult:{}", updateResult);
        log.info("ddns end...");
    }

    /**
     * 是否是windows
     *
     * @param inetAddress
     * @return
     */
    private boolean isWin(InetAddress inetAddress) {
        Properties sysProperties = System.getProperties();
        String osName = sysProperties.getProperty("os.name");
        if (StrUtil.containsIgnoreCase(osName, "windows")
                && inetAddress instanceof Inet6Address
                && !inetAddress.isLinkLocalAddress()
                && !inetAddress.isLoopbackAddress()) {
            return true;
        }
        return false;
    }

    /**
     * 是否是linux
     *
     * @param inetAddress
     * @return
     */
    private boolean isLinux(InetAddress inetAddress) {
        if (inetAddress instanceof Inet6Address && !inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()) {
            return true;
        }
        return false;
    }

    /**
     * 获取windows系统真实的ipv6地址
     *
     * @param hostAddress
     * @return
     */
    private String getWinRealIpv6(String hostAddress) {
        String[] v6Split = hostAddress.split(":");
        String lastValue = v6Split[v6Split.length - 1];
        if (!NumberUtil.isNumber(lastValue)) {
            return null;
        }
        return excludeZeroColon(hostAddress, v6Split);
    }


    /**
     * 获取linux真实的ipv6地址
     *
     * @param hostAddress
     * @return
     */
    private String getLinuxRealIpv6(String hostAddress) {
        String[] networkSplit = hostAddress.split("%");
        String ipv6 = networkSplit[0];
        String[] v6Split = ipv6.split(":");
        String lastValue = v6Split[v6Split.length - 1];
        if (NumberUtil.isNumber(lastValue)) {
            return null;
        }
        return excludeZeroColon(ipv6, v6Split);
    }

    private String excludeZeroColon(String ipv6, String[] v6Split) {
        List<Integer> indexes = getZeroIndexes(v6Split);
        if (CollectionUtils.isEmpty(indexes)) {
            return ipv6;
        }
        ArrayList<String> items = new ArrayList<>(Arrays.asList(v6Split));
        for (Integer index : indexes) {
            items.remove("0");
        }
        int addIndex = indexes.get(0).intValue();
        items.add(addIndex, ":");
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            if (item.equals(items.get(items.size() - 1)) || item.equals(":")) {
                builder.append(item);
                continue;
            }
            builder.append(item + ":");
        }
        return builder.toString();
    }

    /**
     * 获取值为0的多个下标
     *
     * @param splits
     * @return
     */
    private List<Integer> getZeroIndexes(String[] splits) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < splits.length; i++) {
            if (splits[i].equals("0")) {
                list.add(i);
            }
        }
        return list;
    }
}
