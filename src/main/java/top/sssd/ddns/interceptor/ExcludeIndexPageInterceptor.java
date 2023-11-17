package top.sssd.ddns.interceptor;

import cn.hutool.core.net.NetUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;
import sun.net.util.IPAddressUtil;
import top.sssd.ddns.config.ApplicationContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author sssd
 * @careate 2023-11-17-0:03
 */
public class ExcludeIndexPageInterceptor implements HandlerInterceptor{



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
        Environment environment = applicationContext.getEnvironment();
        String property = environment.getProperty("ddns4j.publicAccessDisabled");
        boolean publicAccessDisabled = Boolean.parseBoolean(property);
        if (publicAccessDisabled) {
            String remoteAddr = request.getRemoteAddr();
            String requestURI = request.getRequestURI();

            if (IPAddressUtil.isIPv6LiteralAddress(remoteAddr)) {
                if(!isIPv6Private(remoteAddr) && requestURI.equals("/index.html")){
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }else{
                boolean innerIP = NetUtil.isInnerIP(remoteAddr);
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
}
