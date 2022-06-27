package service;


import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.nio.charset.StandardCharsets;

import static model.Type.SUBTASK;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER_LINE = "id,type,name,status,description,epic";
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
                allTasks.put(task.getId(), task);
                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = (Epic) task;
                        manager.epics.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        Epic epicOfSubTask = manager.epics.get(subTask.getEpicId());
                        manager.subTasks.put(subTask.getId(), subTask);
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
                + task.getDescription() + "," + epicId;
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

            switch (type) {
                case SUBTASK:
                    task = new SubTask(name, description, status, Integer.parseInt(values[5].trim()));
                    task.setId(id);
                    break;
                case EPIC:
                    task = new Epic(name, description);
                    task.setId(id);
                    break;
                case TASK:
                    task = new Task(name, description, status);
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
    public static void main(String[] args) throws IOException {
        Path testFile = Files.createTempFile("tmstorage", ".csv");
        testFile.toFile().deleteOnExit();
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), testFile.toFile());
        System.out.println("Поехали! Файл: "  + testFile);

        Task task;
        SubTask subTask;
        Epic epic;
        List<Task> history;
        /*Create tasks*/
        manager.createTask(new Task("Покупка билетов", "Купить билеты", Status.NEW));
        manager.createTask(new Task("Бронирование жилья", "Арендовать квартиру", Status.NEW));

        epic = new Epic("Релокация", "Переехать жить и работать в другую страну");
        manager.createEpic(epic);
        subTask = new SubTask("Подготовка документов", "Подготовить все документы", Status.NEW, epic.getId());
        manager.createSubTask(subTask);
        subTask = new SubTask("Устройство на работу", "Устроиться на работу в новой локации", Status.NEW, epic.getId());
        manager.createSubTask(subTask);
        subTask = new SubTask("Открытие счёта", "Открыть счёт в банке", Status.NEW, epic.getId());
        manager.createSubTask(subTask);

        epic = new Epic("Получение гражданства", "Получить гражданство");
        manager.createEpic(epic);

        /*Print tasks*/
        System.out.println(manager.getTask(1));
        System.out.println(manager.getTask(2));
        System.out.println(manager.getEpic(3));
        System.out.println(manager.getSubTask(4));
        System.out.println(manager.getSubTask(5));
        System.out.println(manager.getSubTask(6));
        System.out.println(manager.getEpic(7));
        System.out.println(manager.getTask(2));
        System.out.println(manager.getSubTask(4));
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History: " + history.get(i));
        }
        System.out.println("Load from file");
        TaskManager managerFromFile = loadFromFile(Managers.getDefaultHistory(), testFile.toFile());
        history = managerFromFile.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History from file: " + history.get(i));
        }
    }
}
