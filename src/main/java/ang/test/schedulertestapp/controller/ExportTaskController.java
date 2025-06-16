package ang.test.schedulertestapp.controller;

import ang.test.schedulertestapp.QueryService;
import ang.test.schedulertestapp.persistence.ExportTask;
import ang.test.schedulertestapp.persistence.ExportTaskRepository;
import ang.test.schedulertestapp.persistence.TaskType;
import ang.test.schedulertestapp.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class ExportTaskController {

    private final ExportTaskRepository exportTaskRepository;
    private final SchedulerService schedulerService;
    private final QueryService queryService;

    @Autowired
    public ExportTaskController(ExportTaskRepository exportTaskRepository, SchedulerService schedulerService, QueryService queryService) {
        this.exportTaskRepository = exportTaskRepository;
        this.schedulerService = schedulerService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ExportTask> createTask(@RequestBody ExportTask exportTask) {
        // Save the task to DB first
        ExportTask savedTask = exportTaskRepository.save(exportTask);
        // Actually schedule it then
        scheduleTask(savedTask);
        return ResponseEntity.ok(savedTask);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ExportTask> getTask(@PathVariable Long id) {
        return exportTaskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<ExportTask> getAllTasks() {
        return exportTaskRepository.findAll();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelTask(@PathVariable Long id) {
        boolean cancelled = schedulerService.cancelTask(id);
        if (cancelled) {
            return ResponseEntity.ok("Task " + id + " cancelled");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task " + id + " not found or already finished");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody ExportTask updatedTask) {
        return exportTaskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setMessage(updatedTask.getMessage());
                    existingTask.setOutputFile(updatedTask.getOutputFile());
                    existingTask.setScheduledTimestamp(updatedTask.getScheduledTimestamp());
                    exportTaskRepository.save(existingTask);

                    boolean updated = schedulerService.updateTask(id, () -> queryService.perform(existingTask));

                    if (updated) {
                        return ResponseEntity.ok("Task " + id + " updated successfully");
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Task " + id + " is running and cannot be updated");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        schedulerService.cancelTask(id); // ensure scheduled task is stopped
        exportTaskRepository.deleteById(id);
        return ResponseEntity.ok("Task " + id + " deleted");
    }

    private void scheduleTask(ExportTask savedTask) {
        if (savedTask.getTaskType().equals(TaskType.ON_DEMAND)) {
            // if no timestamp is provided - schedule right away
            if (savedTask.getScheduledTimestamp() == null || savedTask.getScheduledTimestamp().isBlank())
                schedulerService.scheduleOnDemandTask(savedTask.getId(), () -> queryService.perform(savedTask));
            else {
                CronExpression cronExpression = CronExpression.parse(savedTask.getScheduledTimestamp());
                schedulerService.scheduleOnDemandTask(savedTask.getId(), () -> queryService.perform(savedTask), cronExpression);
            }
        } else if (savedTask.getTaskType().equals(TaskType.SCHEDULED)) {
            CronExpression cronExpression = CronExpression.parse(savedTask.getScheduledTimestamp());
            schedulerService.scheduleCronTask(savedTask.getId(), () -> queryService.perform(savedTask), cronExpression);
        }
    }
}
