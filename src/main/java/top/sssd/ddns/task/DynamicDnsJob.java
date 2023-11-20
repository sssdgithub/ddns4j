package top.sssd.ddns.task;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import top.sssd.ddns.factory.DynamicDnsServiceFactory;
import top.sssd.ddns.model.entity.ChangedLog;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.ChangedLogService;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.service.IParsingRecordService;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author sssd
 * @created 2023-05-02-11:51
 */
@Slf4j
public class DynamicDnsJob implements Job {


    @Resource
    private IParsingRecordService parsingRecordService;

    @Resource
    private ChangedLogService changedLogService;


    @Override
    public void execute(JobExecutionContext context) {
        Object executeParams = context.getJobDetail().getJobDataMap().get("executeParams");
        ParsingRecord parsingRecord = (ParsingRecord) executeParams;

        DynamicDnsService dynamicDnsService = DynamicDnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());
        String dnsIp   = null;
        try {
            dnsIp = dynamicDnsService.getIpBySubDomainWithType(parsingRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String nowIp = parsingRecordService.getIp(parsingRecord);
        if(nowIp.equals(dnsIp)){
            log.info("域名为:{}的记录,域名服务商中的ip:{}与现在的ip:{},未发生改变",parsingRecord.getDomain(),dnsIp,nowIp);
            String content = String.format("域名为:%s的记录,未发生改变", parsingRecord.getDomain());
            insertLog(content);
            return;
        }
        try {
            parsingRecordService.modify(parsingRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("域名为:{}的记录,已将域名服务商中的ip:{},修改为现在的ip:{},更新成功",parsingRecord.getDomain(),dnsIp,nowIp);
        String content = String.format("域名为:%s的记录,更新成功",parsingRecord.getDomain());
        insertLog(content);
    }


    public void insertLog(String content){
        ChangedLog changedLog = new ChangedLog();
        changedLog.setContent(content);
        changedLog.setInsertDate(LocalDateTime.now());
        changedLogService.save(changedLog);
    }


}
