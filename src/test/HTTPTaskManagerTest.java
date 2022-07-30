import http.HttpTaskServer;
import http.KVServer;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HttpTaskManager;
import service.InMemoryTaskManager;
import service.Managers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    KVServer kvServer;
    HttpTaskManager httpTaskManager;
    HttpTaskServer httpTaskServer;

    @BeforeEach
    void beforeEachTest() {
        try {
            kvServer = new KVServer();
            kvServer.start();
            httpTaskManager = new HttpTaskManager("http://localhost:8078", Managers.getDefaultHistory(), true );
            taskManager = httpTaskManager;
            httpTaskServer = new HttpTaskServer(taskManager);
            httpTaskServer.startHttpServer();

        }
        catch(IOException e) {
            e.printStackTrace();
        }
        super.beforeEachTest();
    }
    @AfterEach
    public void clearTaskMapEpicMapSubTaskMap() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();

        kvServer.stopKVServer();
        httpTaskServer.stopHttpServer();
    }

    @Test
    void loadFromFileTest() {
        Map<Integer, Task> tasks = taskManager.getTasks();
        Map<Integer, Epic> epics = taskManager.getEpics();
        Map<Integer, SubTask> subTasks = taskManager.getSubTasks();
        List<Task> history = taskManager.getHistory();
        httpTaskManager.loadFromServer(Managers.getDefaultHistory());
        assertEquals(tasks, taskManager.getTasks(), "Map of tasks after loading is corrupted");
        assertEquals(epics, taskManager.getEpics(), "Map of epics after loading is corrupted");
        assertEquals(subTasks, taskManager.getSubTasks(), "Map of subTasks after loading is corrupted");
        assertEquals(history, taskManager.getHistory(), "List of history after loading is corrupted");
    }

}

