package service;

import exception.TaskOverlapException;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, SubTask> subTasks;
    protected final Map<Integer, Epic> epics;
    protected final HistoryManager historyManager;
    protected int id;
    protected final TreeSet<Task> prioritizedTasks;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        subTasks = new HashMap<>();
        epics = new HashMap<>();
        this.historyManager = historyManager;
        prioritizedTasks = new TreeSet<>(Comparator.comparing(
            Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())
        )
        .thenComparingInt(Task::getId));
    }

    protected void checkOverlaping(Task task) {
        for (Task taskPriority : prioritizedTasks) {
            if (taskPriority.getStartTime() == null || task.getStartTime() == null) {
                break;
            }
            if (task.getId() == taskPriority.getId()) {
                continue;
            }
            if (!(
                !taskPriority.getEndTime().isAfter(task.getStartTime()) ||
                !task.getEndTime().isAfter(taskPriority.getStartTime())
                )
            ) {
                throw new TaskOverlapException("Task is overlapped with existed one");
            }
        }
    }

    public Integer getId() {
        return ++id;
    }
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
    @Override
    public Map<Integer, Task> getTasks() { return new HashMap<>(tasks); }
    @Override
    public Map<Integer, SubTask> getSubTasks() {
        return new HashMap<>(subTasks);
    }
    @Override
    public Map<Integer, Epic> getEpics() {
        return new HashMap<>(epics);
    }
    @Override
    public Map<Integer, SubTask> getSubTasksByEpicId(int id) {
        if (epics.get(id) == null) {
            return null;
        } else {
            return epics.get(id).getSubTasksEpic();
        }
    }
    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }
    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (SubTask subTask : epic.getSubTasksEpic().values()) {
                historyManager.remove(subTask.getId());
                prioritizedTasks.remove(subTask);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
        subTasks.clear();
    }
    @Override
    public void deleteAllSubTasks() {
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
            prioritizedTasks.remove(subTask);
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTasksEpic().clear();
            updateStatusEpic(epic);
            setEpicEndTime(epic);
        }
    }
    @Override
    public Task getTask(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }
    @Override
    public SubTask getSubTask(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }
    @Override
    public Epic getEpic(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }
    @Override
    public void createTask(Task task) {
        task.setId(getId());
        checkOverlaping(task);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }
    @Override
    public void createSubTask(SubTask subTask) {
        subTask.setId(getId());
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
    @Override
    public void createEpic(Epic epic) {
        epic.setId(getId());
        epics.put(epic.getId(), epic);
    }
    @Override
    public void deleteTask(int id) {
        if (tasks.get(id) != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
        } else {
            System.out.println("Задача не найдена.");
        }
    }
    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (SubTask subTask : epic.getSubTasksEpic().values()) {
                historyManager.remove(subTask.getId());
                prioritizedTasks.remove(subTask);
                subTasks.remove(subTask.getId());
            }
            historyManager.remove(id);
            epics.remove(id);
        } else {
            System.out.println("Эпик не найден.");
        }
    }
    @Override
    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Epic epic = epics.get(subTask.getEpicId());
            epic.getSubTasksEpic().remove(id);
            updateStatusEpic(epic);
            setEpicEndTime(epic);
            historyManager.remove(id);
            prioritizedTasks.remove(subTask);
            subTasks.remove(id);
        } else {
            System.out.println("Подзадача не найдена.");
        }
    }
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            checkOverlaping(task);
            prioritizedTasks.remove(tasks.get(task.getId()));
            prioritizedTasks.add(task);
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задача не найдена.");
        }
    }
    @Override
    public void updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            checkOverlaping(subTask);
            prioritizedTasks.remove(subTasks.get(subTask.getId()));
            prioritizedTasks.add(subTask);
            subTasks.put(subTask.getId(), subTask);
            Epic epic = epics.get(subTask.getEpicId());
            epic.getSubTasksEpic().put(subTask.getId(), subTask);
            updateStatusEpic(epic);
            setEpicEndTime(epic);
        } else {
            System.out.println("Подзадача не найдена.");
        }
    }
    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateStatusEpic(epic);
            setEpicEndTime(epic);
        } else {
            System.out.println("Эпик не найден.");
        }
    }
    @Override
    public void updateStatusEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            if (epic.getSubTasksEpic().size() == 0) {
                epic.setStatus(Status.NEW);
            } else {
                int countDone = 0;
                int countNew = 0;

                for (SubTask subTask : epic.getSubTasksEpic().values()) {
                    if (subTask.getStatus() == Status.DONE) {
                        countDone++;
                    }
                    if (subTask.getStatus() == Status.NEW) {
                        countNew++;
                    }
                    if (subTask.getStatus() == Status.IN_PROGRESS) {
                        epic.setStatus(Status.IN_PROGRESS);
                        return;
                    }
                }

                if (countDone == epic.getSubTasksEpic().size()) {
                    epic.setStatus(Status.DONE);
                } else if (countNew == epic.getSubTasksEpic().size()) {
                    epic.setStatus(Status.NEW);
                } else {
                    epic.setStatus(Status.IN_PROGRESS);
                }
            }
        } else {
            System.out.println("Эпик не найден.");
        }
    }
    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }
    @Override
    public void setEpicEndTime(Epic epic) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        int duration = 0;
        for (SubTask subTask : epic.getSubTasksEpic().values()) {
            if (subTask.getStartTime() == null || subTask.getEndTime() == null) {
                continue;
            }
            if (startTime == null) {
                startTime = subTask.getStartTime();
            } else {
                if (subTask.getStartTime().isBefore(startTime)) {
                    startTime = subTask.getStartTime();
                }
            }
            if (endTime == null) {
                endTime = subTask.getEndTime();
            } else {
                if (subTask.getEndTime().isAfter(endTime)) {
                    endTime = subTask.getEndTime();
                }
            }
            duration += subTask.getDuration();
        }
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration(duration);
    }
}