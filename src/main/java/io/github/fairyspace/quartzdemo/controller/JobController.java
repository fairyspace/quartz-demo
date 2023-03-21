package io.github.fairyspace.quartzdemo.controller;

import io.github.fairyspace.coolutils.response.Result;
import io.github.fairyspace.quartzdemo.common.JobInfo;
import io.github.fairyspace.quartzdemo.service.IJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/task")
public class JobController {

    @Autowired
    private IJobService jobService;

    @PostMapping(value = "/add")
    public Result addJob(@RequestBody JobInfo jobInfo) {
        int response = jobService.addJob(jobInfo);
        return Result.success(response);
    }

    @GetMapping(value = "/jobs")
    public Result getAllJobs() {
        List<JobInfo> jobInfos = jobService.getAllJobs();
        return Result.success(jobInfos);
    }


    @PostMapping(value = "/pause")
    public Result pauseJob(String name, String group) {
        boolean b = jobService.pauseJob(name, group);
        return Result.success(b);

    }

    @PostMapping(value = "/resume")
    public Result resumeJob(String name, String group) {
        boolean b = jobService.resumeJob(name, group);
        return Result.success(b);
    }

    @PostMapping(value = "/reschedule")
    public Result reScheduleJob(String name, String group, String cron) {
        boolean b = jobService.reScheduleJob(name, group, cron);
        return Result.success(b);

    }

    @PostMapping(value = "/delete")
    public Result deleteJob(String name, String group) {
        boolean b = jobService.deleteJob(name, group);
        return Result.success(b);
    }

}
