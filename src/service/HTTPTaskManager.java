package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.KVTaskClient;
import model.Epic;
import model.SubTask;
import model.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    protected void save() {

        String jsonTasks = gson.toJson(new ArrayList<>(super.getTasks().values()));
        kvTaskClient.put("tasks", jsonTasks);

        String jsonEpics = gson.toJson(new ArrayList<>(super.getEpics().values()));
        kvTaskClient.put("epics", jsonEpics);

        String jsonSubTasks = gson.toJson(new ArrayList<>(super.getSubTasks().values()));
        kvTaskClient.put("subtasks", jsonSubTasks);

        String jsonHistory = gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList()));
        kvTaskClient.put("history", jsonHistory);
    }

    public void loadFromServer(HistoryManager historyManager) {
        deleteAllTasks();
        deleteAllEpics();
        deleteAllSubTasks();

        String jsonTasks = this.kvTaskClient.load("tasks");
        if (!jsonTasks.isEmpty()) {
            ArrayList<Task> tasks = gson.fromJson(jsonTasks, new TypeToken<ArrayList<Task>>() {
            }.getType());
            for (Task task : tasks) {
                createTask(task);
            }
        }

        String jsonEpics = this.kvTaskClient.load("epics");
        if (!jsonEpics.isEmpty()) {
            ArrayList<Epic> epics = gson.fromJson(jsonEpics, new TypeToken<ArrayList<Epic>>() {
            }.getType());
            for (Epic epic : epics) {
                createEpic(epic);
            }
        }

        String jsonSubTasks = this.kvTaskClient.load("subtasks");
        if (!jsonSubTasks.isEmpty()) {
            ArrayList<SubTask> subtasks = gson.fromJson(jsonSubTasks, new TypeToken<ArrayList<SubTask>>() {
            }.getType());
            for (SubTask subtask : subtasks) {
                createSubTask(subtask);
            }
        }

        id = getMaxId();

        String jsonHistory = this.kvTaskClient.load("history");
        if (!jsonHistory.isEmpty()) {
            ArrayList<Integer> taskHistoryIds = gson.fromJson(jsonHistory, new TypeToken<ArrayList<Task>>() {
            }.getType());
            for (Integer id : taskHistoryIds) {
                if (getTasks().containsKey(id)) { historyManager.add(getTask(id)); }
                if (getEpics().containsKey(id)) { historyManager.add(getEpic(id)); }
                if (getTasks().containsKey(id)) { historyManager.add(getTask(id)); }
            }
        }

    }

    private Integer getMaxId() {
        Collection<Integer> merged = Stream.of(tasks.keySet(), epics.keySet(), subTasks.keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return Collections.max(merged);
    }

}
