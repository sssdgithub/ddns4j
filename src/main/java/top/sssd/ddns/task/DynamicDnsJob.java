package top.sssd.ddns.task;

import com.aliyun.alidns20150109.Client;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.IParsingRecordService;
import top.sssd.ddns.utils.AliDnsUtils;

import javax.annotation.Resource;

/**
 * @author sssd
 * @careate 2023-05-02-11:51
 */
@Slf4j
public class DynamicDnsJob implements Job {

    @Resource
    private IParsingRecordService parsingRecordService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Object executeParams = context.getJobDetail().getJobDataMap().get("executeParams");
        ParsingRecord parsingRecord = (ParsingRecord) executeParams;

        Client client = AliDnsUtils.createClient(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
        String dnsIp = AliDnsUtils.getIpBySubDomainWithType(client, parsingRecord.getDomain(), RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()));


        String nowIp = parsingRecordService.getIp(parsingRecord);
        if(dnsIp.equals(nowIp)){
            log.info("域名为:{}的记录,域名服务商中的ip:{}与现在的ip:{},未发生改变",parsingRecord.getDomain(),dnsIp,nowIp);
            return;
        }
        parsingRecordService.modify(parsingRecord);
        log.info("域名为:{}的记录,已将域名服务商中的ip:{},修改为现在的ip:{},更新成功",parsingRecord.getDomain(),dnsIp,nowIp);
    }


}
