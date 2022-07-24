import model.*;
import org.junit.jupiter.api.Test;
import service.TaskManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected SubTask subTaskNEW;
    protected SubTask subTaskIN_PROGRESS;
    protected SubTask subTaskDONE;

    void beforeEachTest() {
        task = new Task("Покупка билетов", "Купить билеты", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 0), 10);
        taskManager.createTask(task);

        epic = new Epic("Релокация", "Переехать жить и работать в другую страну", LocalDateTime.of(2022, 7, 24, 10, 15), 10);
        taskManager.createEpic(epic);

        subTaskNEW = new SubTask("Открытие счёта", "Открыть счёт в банке", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 45), 10, epic.getId());
        taskManager.createSubTask(subTaskNEW);
        subTaskDONE = new SubTask("Устройство на работу", "Устроиться на работу в новой локации", Status.DONE, LocalDateTime.of(2022, 7, 24, 10, 30), 10, epic.getId());
        taskManager.createSubTask(subTaskDONE);
        subTaskIN_PROGRESS = new SubTask("Подготовка документов", "Подготовить все документы", Status.IN_PROGRESS, LocalDateTime.of(2022, 7, 24, 10, 15), 10, epic.getId());
        taskManager.createSubTask(subTaskIN_PROGRESS);
    }

    @Test
    void getTasks() {
        assertNotNull(taskManager.getTasks(), "getTasks() failed");
    }

    @Test
    void getEpics() {
        assertNotNull(taskManager.getEpics(), "getEpics() failed");
    }

    @Test
    void getSubTasks() {
        assertNotNull(taskManager.getSubTasks(), "getSubTasks() failed");
    }

    @Test
    void getSubTasksByEpicId() {
        Map<Integer, SubTask> subTasks = taskManager.getSubTasksByEpicId(2);
        assertNotNull(subTasks, "getSubTasksByEpicId(2) failed");
    }

    @Test
    void getTask() {
        Task task1 = taskManager.getTask(1);
        Task task2 = taskManager.getTask(1000);
        assertEquals(1, task1.getId(),  "getTask(1) failed");
        assertNull(task2, "getTask(1000) returned a task");
    }

    @Test
    void getEpic() {
        Epic epic1 = taskManager.getEpic(2);
        Epic epic2 = taskManager.getEpic(1000);
        assertEquals(2, epic1.getId(),  "getEpic(2) failed");
        assertNull(epic2, "getEpic(1000) returned an epic");
        assertEquals(Type.EPIC, epic1.getType(),  "epic1.getType() failed");
        assertTrue(epic1.equals(epic1),  "epic1.equals(epic1) failed");
        assertEquals(epic1.getStartTime().plusMinutes(30), epic1.getEndTime(), "getEndTime() for epic failed");
        assertEquals(10, epic1.getDuration(), "getDuration() failed");
    }

    @Test
    void getSubTask() {
        SubTask subTask1 = taskManager.getSubTask(3);
        SubTask subTask2 = taskManager.getSubTask(1000);
        assertEquals(3, subTask1.getId(),  "getSubTask(3) failed");
        assertNull(subTask2, "getSubTask(1000) returned a subtask");
        assertEquals(Type.SUBTASK, subTask1.getType(),  "getType() failed");
        assertEquals("SubTask{name='Открытие счёта', description='Открыть счёт в банке', id=3, status=NEW,", subTask1.toString().substring(0, 84),  "toString() failed");
        assertTrue(subTask1.equals(subTask1),  "equals(subTask1) failed");
    }

    @Test
    void createTask() {
        Task savedTask = taskManager.getTask(1);
        assertNotNull(savedTask, "createTask(task) failed");
    }

    @Test
    void createEpic() {
        Epic epic1 = taskManager.getEpic(2);
        assertNotNull(epic1, "createEpic(epic) failed");
    }

    @Test
    void createSubTask() {
        SubTask subTask1 = taskManager.getSubTask(3);
        assertNotNull(subTask1, "createSubTask(subTaskNEW) failed");
        int epicId = subTask1.getEpicId();
        assertEquals(2, epicId, "createSubTask(subTaskNEW) failed in part of epic");
    }

    @Test
    void updateTask() {
        Task task1 = taskManager.getTask(1);
        task1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);
        Task task2 = taskManager.getTask(1);
        assertEquals(task1.getStatus(), task2.getStatus(), "updateTask(task1) failed");
    }

    @Test
    void updateEpic() {
        Epic epic1 = taskManager.getEpic(2);
        SubTask subTask1 = taskManager.getSubTask(3);
        SubTask subTask2 = taskManager.getSubTask(4);
        SubTask subTask3 = taskManager.getSubTask(5);

        assertEquals(Status.IN_PROGRESS, epic1.getStatus(), "Returned not IN_PROGRESS status when not all subtasks IN_PROGRESS");

        subTask2.setStatus(Status.NEW);
        taskManager.updateSubTask(subTask2);
        subTask3.setStatus(Status.NEW);
        taskManager.updateSubTask(subTask3);
        assertEquals(Status.NEW, epic1.getStatus(), "Returned not NEW status when all subtasks NEW");

        subTask2.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask2);
        subTask3.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask3);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus(), "Returned not IN_PROGRESS status when subtasks NEW and DONE");

        subTask1.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask1);
        assertEquals(Status.DONE, epic1.getStatus(), "Returned not DONE status when all subtasks DONE");

        epic1.setStatus(Status.NEW);
        epic1.setName("Epic name");
        epic1.setDescription("Epic description");
        taskManager.updateEpic(epic1);
        assertEquals("Epic name", epic1.getName(), "setName(\"Epic name\") failed");
        assertEquals("Epic description", epic1.getDescription(), "setDescription(\"Epic description\") failed");
        assertEquals(Status.DONE, epic1.getStatus(), "Returned status not according to rule");

        taskManager.deleteSubTask(3);
        taskManager.deleteSubTask(4);
        taskManager.deleteSubTask(5);
        assertEquals(Status.NEW, epic1.getStatus(), "Returned not NEW status when subtask list is empty");
    }

    @Test
    void updateSubTask() {
        SubTask subTask1 = taskManager.getSubTask(3);
        subTask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);
        SubTask subTask2 = taskManager.getSubTask(3);
        assertEquals(subTask1.getStatus(), subTask2.getStatus(), "updateSubTask(subTask1) failed");
    }

    @Test
    void deleteAllTasks() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();
        Map<Integer, Task> tasksEmpty = new HashMap<>();
        assertEquals(tasksEmpty, taskManager.getTasks(), "deleteAllTasks() failed");
        assertEquals(tasksEmpty, taskManager.getSubTasks(), "deleteAllSubTasks() failed");
        assertEquals(tasksEmpty, taskManager.getEpics(), "deleteAllEpics() failed");
    }
    @Test
    public void shouldReturnPrioritizedTasks() {
        Task taskNext, taskPrev;
        TreeSet<Task> sortedTasks = taskManager.getPrioritizedTasks();
        if (sortedTasks.size() > 0) {
            Iterator taskIterator = sortedTasks.iterator();
            taskPrev = sortedTasks.first();
            while (taskIterator.hasNext()) {
                taskNext = (Task) taskIterator.next();
                if (!taskNext.equals(taskPrev)) {
                    assertTrue(taskNext.getStartTime().isAfter(taskPrev.getStartTime()) || taskNext.getStartTime().isEqual(taskPrev.getStartTime()), "getPrioritizedTasks() failed");
                }
                taskPrev = taskNext;
            }
        }
    }

     @Test
    public void shouldThrowExceptionWhenFindIntersectionBetweenTasks() {
    }

}