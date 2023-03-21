package io.github.fairyspace.quartzdemo.service;

import io.github.fairyspace.quartzdemo.common.JobInfo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class JobServiceImp implements IJobService {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private Scheduler scheduler;

    @Override
    public List<JobInfo> getAllJobs() {
        List<JobInfo> jobInfos = new ArrayList<>();
        try {
            List<String> groups = scheduler.getJobGroupNames();
            int i = 0;
            for (String group : groups) {
                GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(group);
                Set<JobKey> jobKeys = scheduler.getJobKeys(groupMatcher);
                for (JobKey jobKey : jobKeys) {
                    /*自定义的JobInfo*/
                    JobInfo jobInfo = new JobInfo();
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    jobInfo.setJobname(jobKey.getName());
                    jobInfo.setJobgroup(jobKey.getGroup());
                    jobInfo.setJobclassname(jobDetail.getJobClass().getName());
                    Trigger jobTrigger = scheduler.getTrigger(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                    if (jobTrigger != null) {
                        Trigger.TriggerState tState = scheduler.getTriggerState(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                        jobInfo.setTriggername(jobKey.getName());
                        jobInfo.setTriggergroup(jobKey.getGroup());
                        try {
                            CronTrigger cronTrigger = (CronTrigger) jobTrigger;
                            jobInfo.setCronexpression(cronTrigger.getCronExpression());
                        } catch (Exception e) {
                            log.info("不是CronTrigger");
                        }
                        if (jobTrigger.getNextFireTime() != null)
                            jobInfo.setNextfiretime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(jobTrigger.getNextFireTime()));
                        jobInfo.setDescription(jobDetail.getDescription());
                        jobInfo.setState(tState.name());
                        jobInfo.setId(i);
                        jobInfos.add(jobInfo);
                        i += 1;
                    } else {
                        jobInfo.setState("OVER");
                        jobInfo.setId(i);
                        jobInfos.add(jobInfo);
                        i += 1;
                    }

                }

            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return jobInfos;
    }

    @Override
    public boolean resumeJob(String jobName, String jobGroup) {
        boolean result = true;
        try {
            scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            result = false;
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public boolean pauseJob(String jobName, String jobGroup) {
        boolean result = true;
        try {
            scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            result = false;
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public boolean reScheduleJob(String jobName, String jobGroup, String cronExpression) {
        boolean result = true;
        try {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(TriggerKey.triggerKey(jobName, jobGroup));
            CronTrigger cronTriggerOld = (CronTrigger) scheduler.getTrigger(TriggerKey.triggerKey(jobName, jobGroup));
            if (!cronTriggerOld.getCronExpression().equals(cronExpression)) {
                CronTrigger cronTriggerNew = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                        .build();
                scheduler.rescheduleJob(TriggerKey.triggerKey(jobName, jobGroup), cronTriggerNew);
                if (triggerState.name().equals("PAUSED"))
                    this.pauseJob(jobName, jobGroup);
            }

        } catch (SchedulerException e) {
            result = false;
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public boolean deleteJob(String jobName, String jobGroup) {
        boolean result = true;
        try {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup));
            for (Trigger trigger : triggers) {
                TriggerKey key = trigger.getKey();
                if (!"PAUSED".equals(scheduler.getTriggerState(key).name()))
                    scheduler.pauseTrigger(key);
                scheduler.unscheduleJob(key);
            }
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            result = false;
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public int addJob(JobInfo jobInfo) {
        int result = 0;
        int isJobExist = this.isJobExist(JobKey.jobKey(jobInfo.getJobname(), jobInfo.getJobgroup()));
        if (isJobExist == 1) {
            result = -1;
            log.info("任务已经存在！");
        } else {
            try {
                JobDetail jobDetail = null;
                if (isJobExist == 0) {
                    jobDetail = scheduler.getJobDetail(JobKey.jobKey(jobInfo.getJobname(), jobInfo.getJobgroup()));
                } else if (isJobExist == -1) {
                    jobDetail = JobBuilder.newJob(
                                    (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobclassname()))
                            .withIdentity(jobInfo.getJobname(), jobInfo.getJobgroup())
                            .withDescription(jobInfo.getDescription())
                            .storeDurably().build();
                }
                //如果jobInfo的cron表达式为空，则创建常规任务，反之创建周期任务
                if (!StringUtils.isEmpty(jobInfo.getCronexpression())) {
                    CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobInfo.getTriggername(), jobInfo.getTriggergroup())
                            .withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getCronexpression()))
                            .build();
                    scheduler.scheduleJob(jobDetail, cronTrigger);
                } else {

                    SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
                    simpleScheduleBuilder.withIntervalInSeconds(jobInfo.getIntervalsecond());
                    if (jobInfo.getRepeatcount() != null && jobInfo.getRepeatcount() >= 0) {
                        simpleScheduleBuilder.withRepeatCount(jobInfo.getRepeatcount());
                    } else {
                        simpleScheduleBuilder.repeatForever();
                    }

                    TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().withIdentity(jobInfo.getTriggername(), jobInfo.getTriggergroup());

                    if (StringUtils.isEmpty(jobInfo.getStartfiretime())) {
                        builder.startNow();
                    } else {
                        builder.startAt(sdf.parse(jobInfo.getStartfiretime()));
                    }

                    if (!StringUtils.isEmpty(jobInfo.getEndfiretime())) {
                        builder.endAt(sdf.parse(jobInfo.getEndfiretime()));
                    }

                    SimpleTrigger trigger = builder.withSchedule(simpleScheduleBuilder).build();

                    scheduler.scheduleJob(jobDetail, trigger);
                }

            } catch (ClassNotFoundException e) {
                result = 1;
                log.error("任务对应的Class类不存在");
            } catch (SchedulerException e) {
                result = 2;
                log.error("任务调度失败");
            } catch (ParseException e) {
                result = 3;
                log.error("时间转换出错");
            }
        }
        return result;
    }

    @Override
    public int isJobExist(JobKey jobKey) {
        int result = 1;
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (jobDetail != null && triggers.size() > 0)
                result = 1;
            else if (jobDetail != null && triggers.size() == 0)
                result = 0;
            else
                result = -1;
        } catch (SchedulerException e) {
            result = -1;
            log.info("任务不存在！");
        }
        return result;
    }
}
