package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import top.sssd.ddns.common.valid.ValidGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 解析记录表
 * </p>
 *
 * @author sssd
 * @since 2023-03-19
 */
@TableName("parsing_record")
@Data
public class ParsingRecord extends AmisPageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(groups = ValidGroup.UpdateGroup.class, message = "id不能为空")
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "服务提供商不能为空,1 阿里云 2 腾讯云 3 cloudflare")
    private Integer serviceProvider;

    @TableField(exist = false)
    private String serviceProviderName;

    @NotBlank(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "服务提供商密钥key不能为空,1 阿里云 2 腾讯云 ")
    private String serviceProviderId;

    @NotBlank(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "服务提供商密钥value不能为空,1 阿里云 2 腾讯云,1 阿里云 2 腾讯云 3 cloudflare")
    private String serviceProviderSecret;

    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "解析类型不能为空,解析类型:1 AAAA 2 A")
    private Integer recordType;

    @TableField(exist = false)
    private String recordTypeName;

    private String ip;

    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "获取ip方式不能为空,获取ip方式: 1 interface 2 network 3 cmd")
    private Integer getIpMode;

    private String getIpModeValue;

    @NotBlank(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "域名不能为空")
    private String domain;

    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "状态不能为空")
    private Integer state;

    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "更新频率不能为空")
    private Integer updateFrequency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;

    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;


}
