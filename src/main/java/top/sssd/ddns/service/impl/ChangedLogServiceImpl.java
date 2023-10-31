package top.sssd.ddns.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.sssd.ddns.mapper.ChangedLogMapper;
import top.sssd.ddns.model.entity.ChangedLog;
import top.sssd.ddns.service.ChangedLogService;

/**
 * @author sssd
 * @careate 2023-10-31-16:44
 */
@Service
public class ChangedLogServiceImpl extends ServiceImpl<ChangedLogMapper, ChangedLog> implements ChangedLogService {
}
