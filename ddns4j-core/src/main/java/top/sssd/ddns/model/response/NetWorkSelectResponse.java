package top.sssd.ddns.model.response;

import lombok.Data;

/**
 * 返回前端下拉框模型
 * @author sssd
 * @created 2023-09-22-15:56
 */
@Data
public class NetWorkSelectResponse {
    /**
     * 下拉框显示的网卡名及ip地址
     */
    private String label;
    /**
     * 真正的ip地址
     */
    private String value;
}
