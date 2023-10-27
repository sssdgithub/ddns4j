package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sssd
 * @careate 2023-10-26-15:40
 */
@Data
public class AmisPageEntity implements Serializable {
    @TableField(exist = false)
    @JsonProperty("page")
    private Long page ;

    @TableField(exist = false)
    @JsonProperty("perPage")
    private Long perPage ;

}
