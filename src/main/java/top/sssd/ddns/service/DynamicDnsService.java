package top.sssd.ddns.service;

import top.sssd.ddns.model.entity.ParsingRecord;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author sssd
 * @created 2023-03-19-21:43
 */
public interface DynamicDnsService {

    /**
     * 通过域名及密钥判断是否存在
     * @param serviceProviderId
     * @param serviceProviderSecret
     * @param subDomain
     * @param recordType
     * @return
     */
    boolean exist(@NotBlank(message = "传入的serviceProviderId不能为空") String serviceProviderId,
                  String serviceProviderSecret,@NotBlank(message = "传入的子域名不能为空") String subDomain,
                  @NotBlank(message = "传入的解析类型不能为空") String recordType) throws Exception;

    /**
     * 新增解析记录
     *
     * @param parsingRecord 解析对象
     */
    void add(@NotNull(message = "传入的解析对象不能为空") ParsingRecord parsingRecord,String ip) throws Exception;

    /**
     * 更新解析记录
     *
     * @param parsingRecord 解析对象
     */
    void update(@NotNull(message = "传入的解析对象不能为空") ParsingRecord parsingRecord,String ip,String recordId) throws Exception;


    /**
     * 获取服务上解析记录ID
     * @param parsingRecord
     * @param ip
     * @return
     */
    String getRecordId(@NotNull(message = "传入的解析对象不能为空") ParsingRecord parsingRecord,String ip) throws Exception;

    /**
     * 根据解析记录Id删除记录
     * @param parsingRecord
     * @param ip
     */
    void remove(@NotNull(message = "传入的解析对象不能为空") ParsingRecord parsingRecord, String ip) throws Exception;


    /**
     * 根据解析记录获取服务商中的ip
     * @param parsingRecord
     * @return
     */
    String getIpBySubDomainWithType(@NotNull(message = "传入的解析对象不能为空") ParsingRecord parsingRecord) throws Exception;
}
