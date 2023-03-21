package io.github.fairyspace.quartzdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class QuartzJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        log.info("------任务名：" + jobDetail.getKey().getName() + ",组名：" +
                jobDetail.getKey().getGroup() + "------我是要执行的定时任务工作内容！");
    }
}
