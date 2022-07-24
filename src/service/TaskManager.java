package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface TaskManager {
    List<Task> getHistory();
    Map<Integer, Task> getTasks();

    Map<Integer, SubTask> getSubTasks();

    Map<Integer, Epic> getEpics();

    Map<Integer, SubTask> getSubTasksByEpicId(int id);

    void printTasks();

    void printEpics();

    void printSubTasks();

    void printSubTasksByEpicId(int id);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    Task getTask(int id);

    SubTask getSubTask(int id);

    Epic getEpic(int id);

    void createTask(Task task);

    void createSubTask(SubTask subTask);

    void createEpic(Epic epic);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubTask(int id);

    void updateTask(Task task);

    void updateSubTask(SubTask subTask);

    void updateEpic(Epic epic);

    void updateStatusEpic(Epic epic);

    TreeSet<Task> getPrioritizedTasks();
}