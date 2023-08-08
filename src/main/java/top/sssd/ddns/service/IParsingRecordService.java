package top.sssd.ddns.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import top.sssd.ddns.common.utils.PageUtils;
import top.sssd.ddns.model.entity.ParsingRecord;

/**
 * <p>
 * 解析记录表 服务类
 * </p>
 *
 * @author sssd
 * @since 2023-03-19
 */
public interface IParsingRecordService extends IService<ParsingRecord> {

    /**
     * 添加解析记录
     * @param parsingRecord
     */
    void add(ParsingRecord parsingRecord) throws Exception;

    void modify(ParsingRecord parsingRecord) throws Exception;

    void delete(Long id) throws Exception;

    PageUtils<ParsingRecord> queryPage(ParsingRecord parsingRecord);

    String getIp(ParsingRecord parsingRecord);
}
