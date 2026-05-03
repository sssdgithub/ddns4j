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

    public static String[] spiltDomain(String domain){
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        return new String[]{resultDomain,subDoMain};
    }

    public static  int findNthOccurrence(String str, String subStr, int n) {
        // 记录出现次数
        int count = 0;
        // 从后往前查找最后一次出现的位置
        int index = str.lastIndexOf(subStr);
        // 如果找到了并且出现次数小于n
        while (index != -1 && count < n) {
            // 继续往前查找下一次出现的位置
            index = str.lastIndexOf(subStr, index - 1);
            // 更新出现次数
            count++;
        }
        // 返回最后一次出现的位置的索引
        return index;
    }

}
