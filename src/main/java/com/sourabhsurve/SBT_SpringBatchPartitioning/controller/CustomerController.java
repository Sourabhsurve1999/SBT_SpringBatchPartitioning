package com.sourabhsurve.SBT_SpringBatchPartitioning.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;


    @PostMapping("/import")
    public ResponseEntity<String> importCustomers() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        return ResponseEntity.ok("Imported " + jobExecution.getStepExecutions()
                .iterator().next().getWriteCount() + " customers.");
    }
}
