package com.mrtlptkn.springbatchdemoapp.sample04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("api/v1/splitJob")
public class SplitJobController {

    private final JobLauncher jobLauncher;
    private final Job splitJob;

    public SplitJobController(JobLauncher jobLauncher, Job splitJob) {
        this.jobLauncher = jobLauncher;
        this.splitJob = splitJob;
    }

    @PostMapping
    public EntityResponse<String> start(@RequestParam("runDate") String runDate) throws Exception {

        var params = new JobParametersBuilder()
                .addString("runDate", runDate)
                .toJobParameters();

        jobLauncher.run(splitJob, params);
        return EntityResponse.fromObject("Split Job started").build();
    }

}
