package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author sssd
 * @careate 2023-10-31-16:43
 */
@Data
@TableName("changed_log")
public class ChangedLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    @TableField("insert_date")
    private LocalDateTime insertDate;
}
