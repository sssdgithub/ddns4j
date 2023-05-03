package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典类型
 * </p>
 *
 * @author sssd
 * @since 2023-03-19
 */
@TableName("sys_dict_type")
@ApiModel(value = "SysDictType对象", description = "字典类型")
@Data
public class SysDictType implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("字典类型")
    private String dictType;

    @ApiModelProperty("字典名称")
    private String dictName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("创建者")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("更新者")
    private Long updater;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateDate;

}
