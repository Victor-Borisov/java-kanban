package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.KVTaskClient;
import model.Epic;
import model.SubTask;
import model.Task;

import java.io.File;
import java.util.ArrayList;

public class HTTPTaskManager extends FileBackedTaskManager {
    private String url;
    private KVTaskClient kvTaskClient;
    private final Gson gson = new Gson();

    public HTTPTaskManager(String url, HistoryManager historyManager, File file, KVTaskClient kvTaskClient) {
        super(historyManager, file);
        this.url = url;
        if (kvTaskClient == null) {
            this.kvTaskClient = new KVTaskClient(url);
        } else {
            this.kvTaskClient = kvTaskClient;
        }
    }
    public KVTaskClient getKVTaskClient() {
        return kvTaskClient;
    }
    @Override
    public void save() {

        String jsonTasks = gson.toJson(new ArrayList<>(super.getTasks().values()));
        kvTaskClient.put("tasks", jsonTasks);

        String jsonEpics = gson.toJson(new ArrayList<>(super.getEpics().values()));
        kvTaskClient.put("epics", jsonEpics);

        String jsonSubTasks = gson.toJson(new ArrayList<>(super.getSubTasks().values()));
        kvTaskClient.put("subtasks", jsonSubTasks);

        String jsonHistory = gson.toJson(new ArrayList<>(super.getHistory()));
        kvTaskClient.put("history", jsonHistory);
    }

    public HTTPTaskManager loadFromServer(HistoryManager historyManager, KVTaskClient kvTaskClient) {
        HTTPTaskManager manager = new HTTPTaskManager(url, historyManager, null, kvTaskClient);
        int maxLoadedId = 0;

        String jsonTasks = this.kvTaskClient.load("tasks");
        if (!jsonTasks.isEmpty()) {
            ArrayList<Task> tasks = gson.fromJson(jsonTasks, new TypeToken<ArrayList<Task>>() {
            }.getType());
            for (Task task : tasks) {
                if (task.getId() > maxLoadedId) {
                    maxLoadedId = task.getId();
                }
                manager.createTask(task);
            }
        }

        String jsonEpics = this.kvTaskClient.load("epics");
        if (!jsonEpics.isEmpty()) {
            ArrayList<Epic> epics = gson.fromJson(jsonEpics, new TypeToken<ArrayList<Epic>>() {
            }.getType());
            for (Epic epic : epics) {
                if (epic.getId() > maxLoadedId) {
                    maxLoadedId = epic.getId();
                }
                manager.createEpic(epic);
            }
        }

        String jsonSubTasks = this.kvTaskClient.load("subtasks");
        if (!jsonSubTasks.isEmpty()) {
            ArrayList<SubTask> subtasks = gson.fromJson(jsonSubTasks, new TypeToken<ArrayList<SubTask>>() {
            }.getType());
            for (SubTask subtask : subtasks) {
                if (subtask.getId() > maxLoadedId) {
                    maxLoadedId = subtask.getId();
                }
                manager.createSubTask(subtask);
            }
        }

        manager.id = maxLoadedId;

        String jsonHistory = this.kvTaskClient.load("history");
        if (!jsonHistory.isEmpty()) {
            ArrayList<Task> taskHistory = gson.fromJson(jsonHistory, new TypeToken<ArrayList<Task>>() {
            }.getType());
            for (Task task : taskHistory) {
                manager.historyManager.add(task);
            }
        }

        return manager;
    }
}
