package top.sssd.ddns.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.sssd.ddns.model.response.NetWorkSelectResponse;
import top.sssd.ddns.service.NetWorkService;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import static top.sssd.ddns.common.constant.DDNSConstant.RECORD_TYPE_A;
import static top.sssd.ddns.common.constant.DDNSConstant.RECORD_TYPE_AAAA;

/**
 * @author sssd
 * @created 2023-09-22-13:59
 */
@Service
@Slf4j
public class NetWorkServiceImpl implements NetWorkService {

    @Override
    public List<NetWorkSelectResponse> networks(Integer recordType) throws SocketException {
        LinkedList<NetWorkSelectResponse> networkList = new LinkedList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (recordType.equals(RECORD_TYPE_A) && addr instanceof Inet4Address) {
                    NetWorkSelectResponse netWorkSelectResponse = new NetWorkSelectResponse();
                    netWorkSelectResponse.setLabel(iface.getName()+"-"+addr.getHostAddress());
                    netWorkSelectResponse.setValue(addr.getHostAddress());
                    networkList.add(netWorkSelectResponse);
                    log.info("IPv4:{}",addr.getHostAddress());
                } else if (recordType.equals(RECORD_TYPE_AAAA) && addr instanceof Inet6Address) {
                    String hostAddress = addr.getHostAddress();
                    if (hostAddress.contains("%")) {
                        continue;
                    }
                    NetWorkSelectResponse netWorkSelectResponse = new NetWorkSelectResponse();
                    netWorkSelectResponse.setLabel(iface.getName()+"-"+addr.getHostAddress());
                    netWorkSelectResponse.setValue(hostAddress);
                    networkList.add(netWorkSelectResponse);
                    log.info("IPv6:{}",addr.getHostAddress());
                }
            }
        }
        return networkList;
    }


}
