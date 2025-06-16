package ang.test.schedulertestapp;

import ang.test.schedulertestapp.persistence.ExportTask;
import ang.test.schedulertestapp.persistence.ExportTaskRepository;
import ang.test.schedulertestapp.persistence.TaskState;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;

@Service
public class QueryService {

    private final ExportTaskRepository taskRepository;

    @Autowired
    public QueryService(ExportTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void perform(ExportTask task) {
        ExportTask freshTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new IllegalStateException("Task not found"));

        // set the state
        updateTaskState(freshTask, TaskState.IN_PROGRESS);

        // write the msg to the file and imitate some other work
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(freshTask.getOutputFile(), true))) {
            writer.write(freshTask.getMessage());
            writer.newLine();

            for (int i = 0; i < 5; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                Thread.sleep(1000);
            }

            updateTaskState(freshTask, TaskState.FINISHED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // important to keep the interrupt
            updateTaskState(freshTask, TaskState.CANCELED);

        } catch (Exception e) {
            updateTaskState(freshTask, TaskState.FAILED);
        }
    }

    private void updateTaskState(ExportTask task, TaskState state) {
        task.setTaskState(state);
        taskRepository.save(task);
    }
}
