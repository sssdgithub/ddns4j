package top.sssd.ddns.task;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.CollectionUtils;
import top.sssd.ddns.model.entity.ChangedLog;
import top.sssd.ddns.service.ChangedLogService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sssd
 * @careate 2023-11-13-23:47
 */
@Slf4j
public class ClearLogJob implements Job {

    @Resource
    private ChangedLogService changedLogService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("每小时清除日志开始...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAog = now.minusDays(3);
        List<ChangedLog> list = changedLogService.lambdaQuery()
                .le(ChangedLog::getInsertDate, dayAog)
                .orderByDesc(ChangedLog::getInsertDate)
                .list();
        if (CollectionUtils.isEmpty(list)){
            log.info("每小时清除日志结束...");
            return;
        }
        List<Long> ids = list.stream().map(ChangedLog::getId).collect(Collectors.toList());
        changedLogService.removeBatchByIds(ids);
        log.info("每小时清除日志结束...");
    }
}
