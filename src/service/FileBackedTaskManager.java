package service;


import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

import static model.Type.SUBTASK;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER_LINE = "id,type,name,status,description,startTime,duration,epic";
    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }
    protected void save() {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(HEADER_LINE);
            writer.newLine();

            for (Task task : getTasks().values()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Task task : getEpics().values()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Task task : getSubTasks().values()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            writer.newLine();
            writer.write(historyManagerToString(getHistory()));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }
    public static FileBackedTaskManager loadFromFile(HistoryManager historyManager, File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, file);
        Map<Integer, Task> allTasks = new HashMap<>();
        int maxLoadedId = 0;

        List<String> list = null;
        try {
            list = Files.readAllLines(file.toPath());
        } catch (IOException exception) {
            //ignore
        }

        if (list == null || list.isEmpty()) {
            return manager;
        }

        //HEADER_LINE.equals(list.get(0)), so start index with 1
        for (int i = 1; i < list.size(); i++) {
            String item = list.get(i);
            if (item.isBlank()) {
                break;
            }
            Task task = taskFromString(item);
            if (task != null) {
                if (task.getId() > maxLoadedId) {
                    maxLoadedId = task.getId();
                }
                allTasks.put(task.getId(), task);
                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        manager.prioritizedTasks.add(task);
                        break;
                    case EPIC:
                        Epic epic = (Epic) task;
                        manager.epics.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        Epic epicOfSubTask = manager.epics.get(subTask.getEpicId());
                        manager.subTasks.put(subTask.getId(), subTask);
                        manager.prioritizedTasks.add(subTask);
                        epicOfSubTask.getSubTasksEpic().put(subTask.getId(), subTask);
                        manager.updateStatusEpic(epicOfSubTask);
                        break;
                }
            }
        }

        if (list.size() > 1) {
            String history = list.get(list.size() - 1);
            List<Integer> ids = historyManagerFromString(history);
            Collections.reverse(ids);
            for (Integer id : ids) {
                manager.historyManager.add(allTasks.get(id));
            }
        }
        manager.id = maxLoadedId;
        return manager;
    }
    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }
    @Override
    public void createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
    }
    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }
    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }
    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }
    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }
    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = super.getSubTask(id);
        save();
        return subTask;
    }
    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }
    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }
    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }
    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }
    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }
    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }
    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }
    private static String taskToString(Task task) {
        String epicId = "";

        if (task == null) {
            return null;
        } else if (task.getType() == SUBTASK) {
            SubTask subTask = (SubTask) task;
            epicId = String.valueOf(subTask.getEpicId());
        }

        return task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getStatus() + ","
                + task.getDescription() + "," + task.getStartTime() + "," + task.getDuration() + "," + epicId;
    }
    private static Task taskFromString(String line) {
        Task task = null;

        if (line != null && !line.trim().isEmpty()) {
            String[] values = line.split(",");

            int id = Integer.parseInt(values[0].trim());
            Type type = Type.valueOf(values[1].trim());
            String name = values[2].trim();
            Status status = Status.valueOf(values[3].trim());
            String description = values[4].trim();
            LocalDateTime startTime;
            if (values[5] == "null") {
                startTime = null;
            } else {
                startTime = LocalDateTime.parse(values[5]);
            }

            switch (type) {
                case SUBTASK:
                    task = new SubTask(name, description, status, startTime, Integer.parseInt(values[6].trim()), Integer.parseInt(values[7].trim()));
                    task.setId(id);
                    break;
                case EPIC:
                    task = new Epic(name, description, startTime, Integer.parseInt(values[6].trim()));
                    task.setId(id);
                    break;
                case TASK:
                    task = new Task(name, description, status, startTime, Integer.parseInt(values[6].trim()));
                    task.setId(id);
                    break;
            }
        }

        return task;
    }
    private static String historyManagerToString(List<Task> taskList) {
        String historyLine = "";
        List<String> historyIds = new ArrayList<>();

        for (Task task : taskList) {
            historyIds.add(String.valueOf(task.getId()));
        }
        historyLine = String.join(",", historyIds);

        return historyLine;
    }

    private static List<Integer> historyManagerFromString(String line) {
        List<Integer> list = new ArrayList<>();

        if (line != null && !line.trim().isEmpty()) {
            String[] values = line.split(",");

            for (String value : values) {
                list.add(Integer.parseInt(value));
            }
        }

        return list;
    }
    private class ManagerSaveException extends RuntimeException {
        public ManagerSaveException() {
            super();
        }
    }
}
