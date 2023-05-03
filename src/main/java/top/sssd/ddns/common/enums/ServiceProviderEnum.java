package top.sssd.ddns.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务商枚举
 * @author sssd
 * @careate 2023-03-20-15:10
 */
public enum ServiceProviderEnum {
    ALI_YUN(1,"阿里云"),
    TENCENT(2,"腾讯云"),
    CLOUD_FLARE(3,"cloudflare");

    public static final List<Map<String, Object>> selectResultList = new ArrayList<>();

    static {
        for (ServiceProviderEnum el : ServiceProviderEnum.values()) {
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put("no",el.getIndex());
            stringObjectHashMap.put("label",el.getName());
            selectResultList.add(stringObjectHashMap);
        }
    }

    private Integer index;
    private String name;

    ServiceProviderEnum(){}

    ServiceProviderEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
