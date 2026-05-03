package top.sssd.ddns.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sssd
 * @created 2023-05-02-11:37
 */
@AllArgsConstructor
@Getter
public enum UpdateFrequencyEnum {

    ONE_MINUTE(1, "1分钟", "0 */1 * * * ?"),
    TWO_MINUTES(2, "2分钟", "0 */2 * * * ?"),
    FIVE_MINUTES(5, "5分钟", "0 */5 * * * ?"),
    TEN_MINUTES(10, "10分钟", "0 */10 * * * ?");

    private final Integer code;
    private final String desc;
    private final String cronExpression;

    /**
     *  通过 code 获取 desc
     * @param code
     * @return
     */
    public static String getDescByCode(Integer code) {
        for (UpdateFrequencyEnum e : UpdateFrequencyEnum.values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        return null;
    }

    /**
     * 通过 desc 获取 code
      * @param desc
     * @return
     */
    public static Integer getCodeByDesc(String desc) {
        for (UpdateFrequencyEnum e : UpdateFrequencyEnum.values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        return null;
    }

    /**
     * 通过 code 获取 cron 表达式
     * @param code
     * @return
     */
    public static String getCronExpressionByCode(Integer code) {
        for (UpdateFrequencyEnum e : UpdateFrequencyEnum.values()) {
            if (e.code.equals(code)) {
                return e.cronExpression;
            }
        }
        return null;
    }
}
