package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sssd
 * @created 2023-04-18-15:13
 */
@Data
public class PageEntity implements Serializable {
    @TableField(exist = false)
    @JsonProperty("page")
    private Long page ;
    @TableField(exist = false)
    @JsonProperty("pageSize")
    private Long pageSize ;
}
