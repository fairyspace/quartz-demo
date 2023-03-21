package io.github.fairyspace.quartzdemo.service;

import io.github.fairyspace.quartzdemo.common.JobInfo;
import org.quartz.JobKey;

import java.util.List;

public interface IJobService {
    public List<JobInfo> getAllJobs();
    //恢复任务
    public boolean resumeJob(String jobName,String jobGroup);
    //停止任务
    public boolean pauseJob(String jobName,String jobGroup);
    //修改任务执行周期表达式
    public boolean reScheduleJob(String jobName,String jobGroup,String cronExpression);
    //删除任务
    public boolean deleteJob(String jobName,String jobGroup);
    //新增任务
    public int addJob(JobInfo jobInfo);
    //判断任务是否存在
    public int isJobExist(JobKey jobKey);
}
