import java.util.HashMap;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
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
        epics.clear();
    }

    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            return tasks.get(id);
        }
        return null;
    }

    public SubTask getSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            return subTasks.get(id);
        }
        return null;
    }

    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            return epics.get(id);
        }
        return null;
    }

    public void createTask(String name, String description) {
        addTaskInList(new Task(name, description, getId(), Status.NEW));
    }

    public void addTaskInList(Task task) {
        tasks.put(task.getId(), task);
    }

    public void createSubTask(int epicId, String name, String description) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            int idSubTask = getId();
            addSubTaskInList(new SubTask(name, description, idSubTask, Status.NEW, epicId));
            SubTask subTask = subTasks.get(idSubTask);
            epic.getSubTasksEpic().put(idSubTask, subTask);
            updateEpic(epics.get(epicId));
        } else {
            System.out.println("Такого эпика не существует.");
        }
    }

    public void addSubTaskInList(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
    }

    public void createEpic(String name, String description) {
        addEpicInList(new Epic(name, description, getId(), Status.NEW));
    }

    public void addEpicInList(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void deleteTask(int id) {
        if (tasks.get(id) != null) {
            tasks.remove(id);
        } else {
            System.out.println("Такой задачи не существует.");
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
            System.out.println("Такого эпика не существует.");
        }
    }

    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Epic epic = epics.get(subTask.getLinkToEpic());
            epic.getSubTasksEpic().remove(id);
            updateEpic(epic);
            subTasks.remove(id);
        } else {
            System.out.println("Такой подзадачи не существует.");
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            addTaskInList(task);
            System.out.println(task);
        } else {
            System.out.println("Такой задачи не существует.");
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            addSubTaskInList(subTask);
            System.out.println(subTask);
            Epic epic = epics.get(subTask.getLinkToEpic());
            epic.getSubTasksEpic().put(subTask.getId(), subTask);
            updateEpic(epic);
        } else {
            System.out.println("Такой подзадачи не существует.");
        }
    }

    public void updateEpic(Epic epic) {
        if (checkEpic(epic.getId())) {
            if (epic.getSubTasksEpic().size() == 0) {
                epics.put(epic.getId(), epic);
            } else if (epic.getSubTasksEpic().size() != 0) {
                int countDone = 0;
                int countNew = 0;

                for (SubTask subTask : epic.getSubTasksEpic().values()) {
                    if (subTask.getStatus() == Status.DONE) {
                        countDone++;
                    }
                    if (subTask.getStatus() == Status.NEW) {
                        countNew++;
                    }
                }

                addEpicInList(epic);

                if (countDone == epic.getSubTasksEpic().size()) {
                    epic.setStatus(Status.DONE);
                } else if (countNew == epic.getSubTasksEpic().size()) {
                    epic.setStatus(Status.NEW);
                } else {
                    epic.setStatus(Status.IN_PROGRESS);
                }
            }
        } else {
            System.out.println("Такой эпик не существует.");
        }
    }

    public boolean checkEpic(int id) {
        return epics.get(id) != null;
    }
}