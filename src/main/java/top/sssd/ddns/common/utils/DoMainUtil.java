package top.sssd.ddns.common.utils;

import java.util.Objects;

/**
 * 域名工具类
 * @author sssd
 * @created 2023-03-20-14:58
 */
public class DoMainUtil {
    private DoMainUtil() {
    }

    public static boolean firstLevel(String domain) {
        if (Objects.isNull(domain) || domain.isEmpty()) {
            return false;
        }
        int count = 0;
        for (char aChar : domain.toCharArray()) {
            if (aChar == '.') {
                count++;
            }
        }
        return count == 1;
    }

}
