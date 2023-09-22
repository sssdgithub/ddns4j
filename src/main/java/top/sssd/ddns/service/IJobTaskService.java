package top.sssd.ddns.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.sssd.ddns.model.entity.JobTask;

import java.util.List;

/**
 * @author sssd
 * @created 2023-05-02-11:05
 */
public interface IJobTaskService extends IService<JobTask> {
    /**
     * 添加任务
     * @param jobTask 任务实体
     * @return boolean
     */
    boolean addJobTask(JobTask jobTask);

    /**
     * 更新任务
     * @param jobTask 任务实体
     * @return boolean
     */
    boolean updateJobTask(JobTask jobTask);

    /**
     * 删除任务
     * @param id 任务ID
     * @return boolean
     */
    boolean deleteJobTask(Integer id);

    /**
     * 获取指定任务
     * @param id 任务ID
     * @return JobTask
     */
    JobTask getJobTaskById(Integer id);

    /**
     * 获取所有任务列表
     * @return List<JobTask>
     */
    List<JobTask> listAllJobTasks();

    /**
     * 启动任务
     * @param id 任务ID
     * @return boolean
     */
    boolean startJobTask(Integer id);

    /**
     * 停止任务
     * @param id 任务ID
     * @return boolean
     */
    boolean stopJobTask(Integer id);
}
