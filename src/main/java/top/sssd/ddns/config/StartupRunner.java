package top.sssd.ddns.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.sssd.ddns.model.entity.JobTask;
import top.sssd.ddns.service.IJobTaskService;
import top.sssd.ddns.task.ClearLogJob;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sssd
 * @careate 2023-11-14-0:17
 */
@Component
public class StartupRunner implements CommandLineRunner {

    @Resource
    private IJobTaskService jobTaskService;

    @Override
    public void run(String... args) throws Exception {
        List<JobTask> list = jobTaskService.lambdaQuery()
                .eq(JobTask::getClassName, ClearLogJob.class.getName())
                .eq(JobTask::getStatus, 1).list();

        if (CollectionUtils.isEmpty(list)) {
            addWithStartTask();
            return;
        }
        jobTaskService.startJobTask(list.get(0).getId());
    }


    public void addWithStartTask() {
        JobTask jobTask = new JobTask();
        jobTask.setName("clearLogJob");
        jobTask.setStatus(1);
        jobTask.setClassName(ClearLogJob.class.getName());
        //每小时执行一次
        jobTask.setCronExpression("0 0 0/1 * * ? ");
        jobTask.setExecuteParams(null);
        jobTaskService.addJobTask(jobTask);
    }
}
