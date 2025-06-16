package ang.test.schedulertestapp.persistence;
import jakarta.persistence.*;

@Entity
public class ExportTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String scheduledTimestamp;

    private String outputFile;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    private TaskState taskState;

    public ExportTask() {}

    public ExportTask(String message, String scheduledTimestamp, String outputFile) {
        this.message = message;
        this.scheduledTimestamp = scheduledTimestamp;
        this.outputFile = outputFile;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getScheduledTimestamp() {
        return scheduledTimestamp;
    }

    public void setScheduledTimestamp(String scheduledTimestamp) {
        this.scheduledTimestamp = scheduledTimestamp;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

}
