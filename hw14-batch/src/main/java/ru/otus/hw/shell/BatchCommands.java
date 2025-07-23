package ru.otus.hw.shell;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.services.IdMappingService;

@ShellComponent
public class BatchCommands {
    
    private final JobLauncher jobLauncher;

    private final Job migrationJob;

    private final IdMappingService idMappingService;
    
    @Autowired
    public BatchCommands(JobLauncher jobLauncher, Job migrationJob, IdMappingService idMappingService) {
        this.jobLauncher = jobLauncher;
        this.migrationJob = migrationJob;
        this.idMappingService = idMappingService;
    }
    
    @ShellMethod(value = "Start migration job", key = {"migrate", "start-migration"})
    public String startMigration(@ShellOption(defaultValue = "false") boolean restart) {
        try {
            if (restart) {
                idMappingService.clearMappings();
                System.out.println("[DEBUG_LOG] Cleared ID mappings for restart");
            }
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("restart", String.valueOf(restart))
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(migrationJob, jobParameters);
            
            return String.format("Migration job started with execution ID: %d, Status: %s", 
                    jobExecution.getId(), jobExecution.getStatus());
            
        } catch (Exception e) {
            return "Failed to start migration job: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Restart migration job", key = {"restart-migration", "restart"})
    public String restartMigration() {
        return startMigration(true);
    }
    
    @ShellMethod(value = "Clear ID mappings cache", key = {"clear-cache", "clear"})
    public String clearCache() {
        idMappingService.clearMappings();
        return "ID mappings cache cleared successfully";
    }
    
    @ShellMethod(value = "Show migration status", key = {"status", "migration-status"})
    public String showStatus() {
        return "Migration utility is ready. Use 'migrate' to start migration or " +
                "'restart' to restart with cleared cache.";
    }
}