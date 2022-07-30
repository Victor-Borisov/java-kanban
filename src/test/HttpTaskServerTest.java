import com.google.gson.reflect.TypeToken;
import http.HttpTaskServer;
import http.KVServer;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HttpTaskManager;
import service.Managers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {
    KVServer kvServer;
    HttpTaskManager httpTaskManager;
    HttpTaskServer httpTaskServer;
    protected Task task;
    protected Epic epic;
    protected SubTask subTaskNEW;
    protected SubTask subTaskIN_PROGRESS;
    protected SubTask subTaskDONE;


    @BeforeEach
    public void addAllTasksEpicsAndSubTasks() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskManager = (HttpTaskManager) Managers.getDefault(Managers.getDefaultHistory());
        httpTaskServer = new HttpTaskServer(httpTaskManager);
        httpTaskServer.startHttpServer();

        task = new Task("Покупка билетов", "Купить билеты", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 0), 10);
        httpTaskManager.createTask(task);

        epic = new Epic("Релокация", "Переехать жить и работать в другую страну", LocalDateTime.of(2022, 7, 24, 10, 15), 10);
        httpTaskManager.createEpic(epic);

        subTaskNEW = new SubTask("Открытие счёта", "Открыть счёт в банке", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 45), 10, epic.getId());
        httpTaskManager.createSubTask(subTaskNEW);
        subTaskDONE = new SubTask("Устройство на работу", "Устроиться на работу в новой локации", Status.DONE, LocalDateTime.of(2022, 7, 24, 10, 30), 10, epic.getId());
        httpTaskManager.createSubTask(subTaskDONE);
        subTaskIN_PROGRESS = new SubTask("Подготовка документов", "Подготовить все документы", Status.IN_PROGRESS, LocalDateTime.of(2022, 7, 24, 10, 15), 10, epic.getId());
        httpTaskManager.createSubTask(subTaskIN_PROGRESS);

        httpTaskManager.getTask(task.getId());
        httpTaskManager.getEpic(epic.getId());
        httpTaskManager.getSubTask(subTaskIN_PROGRESS.getId());
    }
    @AfterEach
    public void clearTaskMapEpicMapSubTaskMap() {
        httpTaskManager.deleteAllTasks();
        httpTaskManager.deleteAllSubTasks();
        httpTaskManager.deleteAllEpics();

        kvServer.stopKVServer();
        httpTaskServer.stopHttpServer();
    }

    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromServer = httpTaskServer.getGson().fromJson(response.body(), Task.class);
        assertEquals(task, taskFromServer);
    }

    @Test
    public void shouldReturnTasksList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type type = new TypeToken<HashMap<Integer, Task>>(){}.getType();
        HashMap taskMap = httpTaskServer.getGson().fromJson(response.body(), type);
        assertEquals(httpTaskManager.getTasks(), taskMap);
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/?id=2"))
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epicFromServer = httpTaskServer.getGson().fromJson(response.body(), Epic.class);
        assertEquals(epic, epicFromServer);
    }

    @Test
    public void shouldReturnEpicsList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type type = new TypeToken<HashMap<Integer, Epic>>() {}.getType();
        HashMap epicMap = httpTaskServer.getGson().fromJson(response.body(), type);
        assertEquals(httpTaskManager.getEpics(), epicMap);
    }

    @Test
    public void shouldReturnSubTaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask?id=3"))
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask subTaskFromServer = httpTaskServer.getGson().fromJson(response.body(), SubTask.class);
        assertEquals(subTaskNEW, subTaskFromServer);
    }

    @Test
    public void shouldReturnSubTasksList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type type = new TypeToken<HashMap<Integer, SubTask>>() {}.getType();
        HashMap subtaskMap = httpTaskServer.getGson().fromJson(response.body(), type);
        assertEquals(httpTaskManager.getSubTasks(), subtaskMap);
    }

    @Test
    public void shouldReturnHistoryListElements() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> historyList = httpTaskServer.getGson().fromJson(response.body(), type);

        List<Task> histList = httpTaskManager.getHistory();

        for (int i = 0; i < historyList.size(); i++) {
            assertEquals(histList.get(i).getId(), historyList.get(i).getId());
            assertEquals(histList.get(i).getName(), historyList.get(i).getName());
            assertEquals(histList.get(i).getDescription(), historyList.get(i).getDescription());
            assertEquals(histList.get(i).getStatus(), historyList.get(i).getStatus());
            assertEquals(histList.get(i).getStartTime(), historyList.get(i).getStartTime());
            assertEquals(histList.get(i).getDuration(), historyList.get(i).getDuration());
        }
    }

    @Test
    public void shouldReturnPrioritizedListElements() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> priorServ = httpTaskServer.getGson().fromJson(response.body(), type);

        List<Task> prioritizedTasks = new ArrayList<>(httpTaskManager.getPrioritizedTasks());

        for (int i = 0; i < prioritizedTasks.size(); i++) {
            assertEquals(prioritizedTasks.get(i).getId(), priorServ.get(i).getId());
            assertEquals(prioritizedTasks.get(i).getName(), priorServ.get(i).getName());
            assertEquals(prioritizedTasks.get(i).getDescription(), priorServ.get(i).getDescription());
            assertEquals(prioritizedTasks.get(i).getStatus(), priorServ.get(i).getStatus());
            assertEquals(prioritizedTasks.get(i).getStartTime(), priorServ.get(i).getStartTime());
            assertEquals(prioritizedTasks.get(i).getDuration(), priorServ.get(i).getDuration());
        }
    }
}
