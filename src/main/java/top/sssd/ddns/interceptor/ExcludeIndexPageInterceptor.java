package top.sssd.ddns.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import sun.net.util.IPAddressUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static top.sssd.ddns.common.constant.DDNSConstant.*;

/**
 * @author sssd
 * @careate 2023-11-17-0:03
 */
public class ExcludeIndexPageInterceptor implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Boolean publicAccessDisabled = publicAccessDisabledMap.get(PUBLIC_ACCESS_DISABLED_KEY);
        if (publicAccessDisabled) {
            String remoteAddr = request.getRemoteAddr();
            String requestURI = request.getRequestURI();

            if (IPAddressUtil.isIPv6LiteralAddress(remoteAddr)) {
                if(!isIPv6Private(remoteAddr) && requestURI.equals("/index.html")){
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }else{
                boolean innerIP = isInternalIP(remoteAddr);
                if (!innerIP && requestURI.equals("/index.html")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isIPv6Private(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInternalIP(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            System.out.println("无法解析 IP 地址: " + ipAddress);
            return false;
        }
    }
}
