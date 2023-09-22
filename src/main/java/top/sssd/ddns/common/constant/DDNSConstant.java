package top.sssd.ddns.common.constant;

/**
 * DDNS常量类
 *
 * @author sssd
 */
public class DDNSConstant {
    private DDNSConstant() {
    }

    public static final String CREATE_DATE = "createDate";
    public static final String UPDATE_DATE = "updateDate";
    public static final String CREATOR = "creator";
    public static final String UPDATER = "updater";
    public static final String[] IPV4_INTERFACE_VALUES = {"https://myip4.ipip.net", "https://ddns.oray.com/checkip", "https://ip.3322.net", "https://4.ipw.cn"};
    public static final String[] IPV6_INTERFACE_VALUES =
            {"https://v6.ip.zxinc.org/getip",
                    "https://v6.ip.zxinc.org/getip",
                    "https://api6.ipify.org",
                    "https://api.ip.sb/ip",
                    "https://api.myip.la",
                    "https://speed.neu6.edu.cn/getIP.php",
                    "https://v6.ident.me",
                    "https://6.ipw.cn"};
    public static final Integer RECORD_TYPE_AAAA = 1;
    public static final Integer RECORD_TYPE_A = 2;

    public static final Integer IP_MODE_INTERFACE = 1;
    public static final Integer IP_MODE_NETWORK = 2;
}
