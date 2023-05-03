package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sssd
 * @careate 2023-04-18-15:13
 */
@Data
public class PageEntity implements Serializable {
    @TableField(exist = false)
    private long page = 1;
    @TableField(exist = false)
    private long pageSize = 10;
}
