package ru.otus.hw.shell;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@Slf4j
@ShellComponent
public class BatchCommands {

    private final JobLauncher jobLauncher;

    private final Job migrationJob;

    @Autowired
    public BatchCommands(JobLauncher jobLauncher, Job migrationJob) {
        this.jobLauncher = jobLauncher;
        this.migrationJob = migrationJob;
    }

    @ShellMethod(value = "Start migration job", key = {"migrate", "start-migration"})
    public String startMigration() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(migrationJob, jobParameters);
            
            return String.format("Migration job started with execution ID: %d, Status: %s", 
                    jobExecution.getId(), jobExecution.getStatus());
            
        } catch (Exception e) {
            return "Failed to start migration job: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Show migration status", key = {"status", "migration-status"})
    public String showStatus() {
        return "Migration utility is ready. Use 'migrate' to start migration.";
    }
}