package ang.test.schedulertestapp;

import static org.junit.jupiter.api.Assertions.*;

import ang.test.schedulertestapp.persistence.ExportTask;
import ang.test.schedulertestapp.persistence.ExportTaskRepository;
import ang.test.schedulertestapp.persistence.TaskState;
import ang.test.schedulertestapp.persistence.TaskType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.*;
@SpringBootTest
@Transactional
class QueryServiceTest {

    @Autowired
    private ExportTaskRepository taskRepository;

    @Autowired
    private QueryService queryService;

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test-output", ".txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    @Transactional
    void testPerformSuccessfulTask() throws Exception {
        String message = "Hello World";
        ExportTask task = new ExportTask();
        task.setMessage(message);
        task.setOutputFile(tempFile.toString());
        task.setTaskType(TaskType.ON_DEMAND);
        task.setTaskState(TaskState.RECEIVED);

        task = taskRepository.save(task);  // Now it has an ID assigned

        queryService.perform(task);

        ExportTask updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskState.FINISHED, updatedTask.getTaskState());
        // Verify file content
        String content = Files.readString(tempFile);
        assertTrue(content.contains(message), "Output file should contain the message");
    }
}

