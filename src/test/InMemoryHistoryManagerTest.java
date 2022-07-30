import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryHistoryManager;
import service.Managers;
import service.TaskManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest extends InMemoryHistoryManager {
    TaskManager taskManager;

    @BeforeEach
    void beforeEachTest() {
        taskManager = Managers.getInMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void getHistoryTest() {
        Task task;
        SubTask subTask;
        Epic epic;
        List<Task> history;

        taskManager.createTask(new Task("Покупка билетов", "Купить билеты", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 0), 10));
        taskManager.createTask(new Task("Бронирование жилья", "Арендовать квартиру", Status.NEW, LocalDateTime.of(2022, 7, 24, 9, 0), 10));

        epic = new Epic("Релокация", "Переехать жить и работать в другую страну", LocalDateTime.of(2022, 7, 24, 10, 15), 10);

        taskManager.createEpic(epic);
        subTask = new SubTask("Подготовка документов", "Подготовить все документы", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 45), 10, epic.getId());
        taskManager.createSubTask(subTask);
        subTask = new SubTask("Устройство на работу", "Устроиться на работу в новой локации", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 30), 10, epic.getId());
        taskManager.createSubTask(subTask);
        subTask = new SubTask("Открытие счёта", "Открыть счёт в банке", Status.NEW, LocalDateTime.of(2022, 7, 24, 10, 15), 10, epic.getId());
        taskManager.createSubTask(subTask);

        epic = new Epic("Получение гражданства", "Получить гражданство", LocalDateTime.now(), 10);
        taskManager.createEpic(epic);

        taskManager.getTask(1);
        taskManager.getTask(2);
        taskManager.getEpic(3);
        taskManager.getSubTask(4);
        taskManager.getSubTask(5);
        taskManager.getSubTask(6);
        taskManager.getEpic(7);
        taskManager.getTask(2);
        taskManager.getSubTask(4);

        assertEquals(7, taskManager.getHistory().size(), "getHistory() or add() or remove() failed");
    }
}
