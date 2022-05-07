import java.util.HashMap;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();

    public HashMap<Integer, Task> getTasks() {
        return new HashMap<>(tasks);
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return new HashMap<>(subTasks);
    }

    public HashMap<Integer, Epic> getEpics() {
        return new HashMap<>(epics);
    }

    public HashMap<Integer, SubTask> getSubTasksByEpicId(int id) {
        if (epics.get(id) == null) {
            return null;
        } else {
            return epics.get(id).getSubTasksEpic();
        }
    }

    private int id;

    public int getId() {
        return ++id;
    }

    public void printTasks() {
        if (tasks.size() == 0) System.out.println("Список задач пуст.");
        for (Task name : tasks.values()) {
            System.out.println("Task.name: " + name.getName());
        }
    }

    public void printEpics() {
        if (epics.size() == 0) System.out.println("Список эпиков пуст.");
        for (Epic name : epics.values()) {
            System.out.println("Epic.name: " + name.getName());
        }
    }

    public void printSubTasks() {
        if (subTasks.size() == 0) System.out.println("Список подзадач пуст.");
        for (SubTask name : subTasks.values()) {
            System.out.println("SubTask.name: " + name.getName());
        }
    }

    public void printSubTasksByEpicId(int id) {
        if (epics.get(id) == null) {
            System.out.println("Список подзадач пуст.");
        } else {
            System.out.println(epics.get(id).getSubTasksEpic());
        }
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    public void deleteAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.setSubTasksEpic(null);
            updateStatusEpic(epic);
        }
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public SubTask getSubTask(int id) {
        return subTasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public void createTask(Task task) {
        task.setId(getId());
        tasks.put(task.getId(), task);
    }

    public void createSubTask(SubTask subTask) {
        subTask.setId(getId());
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            subTasks.put(subTask.getId(), subTask);
            epic.getSubTasksEpic().put(subTask.getId(), subTask);
            updateStatusEpic(epic);
        } else {
            System.out.println("Эпик не найден.");
        }
    }

    public void createEpic(Epic epic) {
        epic.setId(getId());
        epics.put(epic.getId(), epic);
    }

    public void deleteTask(int id) {
        if (tasks.get(id) != null) {
            tasks.remove(id);
        } else {
            System.out.println("Задача не найдена.");
        }
    }

    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (SubTask value : epic.getSubTasksEpic().values()) {
                subTasks.remove(value.getId());
            }
            epics.remove(id);
        } else {
            System.out.println("Эпик не найден.");
        }
    }

    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Epic epic = epics.get(subTask.getEpicId());
            epic.getSubTasksEpic().remove(id);
            updateStatusEpic(epic);
            subTasks.remove(id);
        } else {
            System.out.println("Подзадача не найдена.");
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задача не найдена.");
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask);
            Epic epic = epics.get(subTask.getEpicId());
            epic.getSubTasksEpic().put(subTask.getId(), subTask);
            updateStatusEpic(epic);
        } else {
            System.out.println("Подзадача не найдена.");
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateStatusEpic(epic);
        } else {
            System.out.println("Эпик не найден.");
        }
    }
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

                epics.put(epic.getId(), epic);

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

}