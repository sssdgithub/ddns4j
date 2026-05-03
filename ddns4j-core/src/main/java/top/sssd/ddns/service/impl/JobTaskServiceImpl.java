package top.sssd.ddns.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.mapper.JobTaskMapper;
import top.sssd.ddns.model.entity.JobTask;
import top.sssd.ddns.service.IJobTaskService;

import java.util.List;

/**
 * @author sssd
 * @created 2023-05-02-11:06
 */
@Service
@Slf4j
public class JobTaskServiceImpl extends ServiceImpl<JobTaskMapper, JobTask> implements IJobTaskService {
    @Autowired
    private Scheduler scheduler;

    @Override
    public boolean addJobTask(JobTask jobTask) {
        boolean result = this.save(jobTask);
        if (result) {
            JobKey jobKey = new JobKey(jobTask.getName(), jobTask.getGroupName());
            TriggerKey triggerKey = new TriggerKey(jobTask.getName(), jobTask.getGroupName());
            try {
                JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(jobTask.getClassName()))
                        .withIdentity(jobKey)
                        .build();
                jobDetail.getJobDataMap().put("executeParams", jobTask.getExecuteParams());
                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobTask.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing();

                CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey)
                        .withSchedule(cronScheduleBuilder)
                        .startNow()
                        .build();

                scheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (ClassNotFoundException | SchedulerException e) {
                log.error("addJobTask failed", e);
                throw new BizException("addJobTask failed");
            }
        }
        return result;
    }

    @Override
    public boolean updateJobTask(JobTask jobTask) {
        boolean result = this.updateById(jobTask);
        if (result) {
            JobKey jobKey = new JobKey(jobTask.getName(), jobTask.getGroupName());
            TriggerKey triggerKey = new TriggerKey(jobTask.getName(), jobTask.getGroupName());
            try {
                CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);

                if (cronTrigger == null) {
                    log.error("updateJobTask failed: trigger not found");
                    throw new BizException("updateJobTask failed: trigger not found");
                }

                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                jobDetail.getJobDataMap().put("executeParams", jobTask.getExecuteParams());

                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobTask.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing();

                cronTrigger = cronTrigger.getTriggerBuilder()
                        .withIdentity(triggerKey)
                        .withSchedule(cronScheduleBuilder)
                        .build();

                scheduler.rescheduleJob(triggerKey, cronTrigger);
            } catch (SchedulerException e) {
                log.error("updateJobTask failed", e);
                throw new BizException("updateJobTask failed");
            }
        }
        return result;
    }

    @Override
    public boolean deleteJobTask(Integer id) {
        JobTask jobTask = this.getById(id);
        if (jobTask != null) {
            JobKey jobKey = new JobKey(jobTask.getName(), jobTask.getGroupName());
            TriggerKey triggerKey = new TriggerKey(jobTask.getName(), jobTask.getGroupName());
            try {
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                log.error("deleteJobTask failed", e);
                throw new BizException("deleteJobTask failed");
            }
        }
        return this.removeById(id);
    }

    @Override
    public JobTask getJobTaskById(Integer id) {
        return this.getById(id);
    }

    @Override
    public List<JobTask> listAllJobTasks() {
        return this.list();
    }

    @Override
    public boolean startJobTask(Integer id) {
        JobTask jobTask = this.getById(id);
        if (jobTask != null) {
            JobKey jobKey = new JobKey(jobTask.getName(), jobTask.getGroupName());
            try {
                scheduler.resumeJob(jobKey);
            } catch (SchedulerException e) {
                log.error("startJobTask failed", e);
                throw new BizException("startJobTask failed");
            }
            jobTask.setStatus(1);
            this.updateById(jobTask);
            return true;
        }
        return false;
    }

    @Override
    public boolean stopJobTask(Integer id) {
        JobTask jobTask = this.getById(id);
        if (jobTask != null) {
            JobKey jobKey = new JobKey(jobTask.getName(), jobTask.getGroupName());
            try {
                scheduler.pauseJob(jobKey);
            } catch (SchedulerException e) {
                log.error("stopJobTask failed", e);
                throw new BizException("stopJobTask failed");
            }
            jobTask.setStatus(0);
            this.updateById(jobTask);
            return true;
        }
        return false;
    }
}
