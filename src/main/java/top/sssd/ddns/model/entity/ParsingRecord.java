package top.sssd.ddns.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "ParsingRecord对象", description = "解析记录表")
@Data
public class ParsingRecord extends PageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @NotNull(groups = ValidGroup.UpdateGroup.class, message = "id不能为空")
    private Long id;

    @ApiModelProperty("服务提供商1 阿里云 2 腾讯云 3 cloudflare")
    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "服务提供商不能为空,1 阿里云 2 腾讯云 3 cloudflare")
    private Integer serviceProvider;

    @NotBlank(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "服务提供商密钥key不能为空,1 阿里云 2 腾讯云 ")
    private String serviceProviderId;

    @NotBlank(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "服务提供商密钥value不能为空,1 阿里云 2 腾讯云,1 阿里云 2 腾讯云 3 cloudflare")
    private String serviceProviderSecret;

    @ApiModelProperty("解析类型:1 AAAA 2 A")
    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "解析类型不能为空,解析类型:1 AAAA 2 A")
    private Integer recordType;

    @ApiModelProperty("真实ip")
    private String ip;

    @ApiModelProperty("获取ip方式: 1 interface 2 network 3 cmd")
    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "获取ip方式不能为空,获取ip方式: 1 interface 2 network 3 cmd")
    private Integer getIpMode;

    @ApiModelProperty("当为interface时 https://myip4.ipip.net, https://ddns.oray.com/checkip, https://ip.3322.net, https://4.ipw.cn	当为network时 是网卡信息	当为cmd时 是bash或shell命令	")
    private String getIpModeValue;

    @ApiModelProperty("域名")
    @NotBlank(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "域名不能为空")
    private String domain;

    @ApiModelProperty("1 启用 0 禁用")
    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "状态不能为空")
    private Integer state;

    @ApiModelProperty("单位:分钟 1分钟 2分钟 5分钟 10分钟")
    @NotNull(groups = {ValidGroup.SaveGroup.class, ValidGroup.UpdateGroup.class}, message = "更新频率不能为空")
    private Integer updateFrequency;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;

    @ApiModelProperty("创建者")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("更新者")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;


}
