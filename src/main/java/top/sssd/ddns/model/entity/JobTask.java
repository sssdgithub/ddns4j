package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sssd
 * @created 2023-05-02-11:01
 */
@Data
@TableName("job_task")
public class JobTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    @TableField("group_name")
    private String groupName;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("class_name")
    private String className;

    private String description;

    private Integer status;

    @TableField(exist = false)
    private transient Object executeParams;
}

