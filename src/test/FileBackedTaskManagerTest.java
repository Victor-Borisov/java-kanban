import model.Epic;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FileBackedTaskManager;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static service.FileBackedTaskManager.loadFromFile;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    Path testFile;

    @BeforeEach
    void setUpFileBacked() throws IOException {
        testFile = Files.createTempFile("tmstorage", ".csv");
        testFile.toFile().deleteOnExit();
        taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), testFile.toFile());
        super.beforeEachTest();
    }

    @Test
    void loadFromFileTest() {

        Task task1 = taskManager.getTask(1);
        TaskManager taskManagerLoaded = loadFromFile(Managers.getDefaultHistory(), testFile.toFile());
        Task task2 = taskManagerLoaded.getTask(1);
        assertEquals(task1, task2, "loadFromFile or save failed");
        assertEquals(task1.getStartTime(), task2.getStartTime(), "getStartTime() after loading failed");
        assertEquals(task1.getDuration(), task2.getDuration(), "getDuration() after loading failed");
        assertEquals(task1.getEndTime(), task2.getEndTime(), "getEndTime() after loading failed");

        Epic epic1 = taskManager.getEpic(2);
        taskManager.deleteAllSubTasks();
        TaskManager taskManagerLoadedEpicWithoutSubTasks = loadFromFile(Managers.getDefaultHistory(), testFile.toFile());
        assertEquals(epic1, taskManagerLoadedEpicWithoutSubTasks.getEpic(2), "loadFromFile for epic without subtasks failed");

        Map<Integer, Task> tasksEmpty = new HashMap<>();
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        TaskManager taskManagerLoadedEmpty = loadFromFile(Managers.getDefaultHistory(), testFile.toFile());
        assertEquals(tasksEmpty, taskManagerLoadedEmpty.getTasks(), "loadFromFile for empty task list failed");
        List<Task> historyEmpty = new ArrayList<>();
        assertEquals(historyEmpty, taskManagerLoadedEmpty.getHistory(), "loadFromFile for empty history failed");
    }


}
