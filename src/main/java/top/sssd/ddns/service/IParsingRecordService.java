package top.sssd.ddns.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.sssd.ddns.common.utils.AmisPageUtils;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.model.response.NetWorkSelectResponse;

import java.net.SocketException;
import java.util.List;

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

//    PageUtils<ParsingRecord> queryPage(ParsingRecord parsingRecord);

    AmisPageUtils<ParsingRecord> queryPage(ParsingRecord parsingRecord);

    String getIp(ParsingRecord parsingRecord);


    List<NetWorkSelectResponse> getModeIpValue(Integer getIpMode,Integer recordType) throws SocketException;
}
