package top.sssd.ddns.service;

import top.sssd.ddns.model.response.NetWorkSelectResponse;

import java.net.SocketException;
import java.util.List;

/**
 * @author sssd
 * @created 2023-09-22-13:56
 */
public interface NetWorkService {

    /**
     * 获取网卡信息
     * @param recordType
     * @return 返回有效ip数组
     * @throws SocketException
     */
    List<NetWorkSelectResponse> networks(Integer recordType) throws SocketException;
}
