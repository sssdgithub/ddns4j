package top.sssd.ddns.common.enums;

/**
 * 服务商枚举
 * @author sssd
 * @created 2023-03-20-15:10
 */
public enum ServiceProviderEnum {
    ALI_YUN(1,"阿里云"),
    TENCENT(2,"腾讯云"),
    CLOUD_FLARE(3,"cloudflare"),
    HUAWEI_YUN(4,"华为云");

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

    public String getName() {
        return name;
    }

    public static String getNameByIndex(Integer index) {
        for (ServiceProviderEnum el : ServiceProviderEnum.values()) {
            if (el.getIndex().equals(index)) {
                return el.getName();
            }
        }
        return null;
    }
}
