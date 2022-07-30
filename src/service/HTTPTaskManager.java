package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.KVTaskClient;
import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpTaskManager extends FileBackedTaskManager {
    private KVTaskClient kvTaskClient;
    private final Gson gson = new Gson();

    public HttpTaskManager(String url, HistoryManager historyManager) {
        this(url, historyManager, false);
    }
    public HttpTaskManager(String url, HistoryManager historyManager, boolean isLoadNeeded) {
        super(historyManager, null);
        this.kvTaskClient = new KVTaskClient(url);
        if (isLoadNeeded) {
            loadFromServer();
        }
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

    private void loadFromServer() {
        String jsonTasks = this.kvTaskClient.load("tasks");
        if (!jsonTasks.isEmpty()) {
            tasks.clear();
            ArrayList<Task> taskList = gson.fromJson(jsonTasks, new TypeToken<ArrayList<Task>>() {
            }.getType());
            for (Task task : taskList) {
                tasks.put(task.getId(), task);
                prioritizedTasks.add(task);
            }
        }

        String jsonEpics = this.kvTaskClient.load("epics");
        if (!jsonEpics.isEmpty()) {
            epics.clear();
            ArrayList<Epic> epicList = gson.fromJson(jsonEpics, new TypeToken<ArrayList<Epic>>() {
            }.getType());
            for (Epic epic : epicList) {
                epics.put(epic.getId(), epic);
            }
        }

        String jsonSubTasks = this.kvTaskClient.load("subtasks");
        if (!jsonSubTasks.isEmpty()) {
            subTasks.clear();
            ArrayList<SubTask> subTaskList = gson.fromJson(jsonSubTasks, new TypeToken<ArrayList<SubTask>>() {
            }.getType());
            for (SubTask subTask : subTaskList) {
                Epic epic = epics.get(subTask.getEpicId());
                if (epic != null) {
                    checkOverlaping(subTask);
                    subTasks.put(subTask.getId(), subTask);
                    prioritizedTasks.add(subTask);
                    epic.getSubTasksEpic().put(subTask.getId(), subTask);
                    updateStatusEpic(epic);
                    setEpicEndTime(epic);
                } else {
                    System.out.println("Эпик не найден.");
                }

            }
        }

        id = getMaxId();

        HistoryManager historyManager = getHistoryManager();
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

    private int getMaxId() {
        Collection<Integer> merged = Stream.of(tasks.keySet(), epics.keySet(), subTasks.keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (merged.size() == 0) {
            return 0;
        } else {
            return Collections.max(merged);
        }
    }

}
