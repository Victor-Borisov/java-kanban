import model.Epic;
import model.SubTask;
import model.Task;
import service.Managers;
import service.TaskManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistory());
        Task task;
        SubTask subTask;
        Epic epic;
        List<Task> history;
        /*Create tasks*/
        /*manager.createTask(new Task("Покупка билетов", "Купить билеты", Status.NEW));
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
        manager.createEpic(epic);*/

        /*Print tasks*/
        /*System.out.println(manager.getTask(1));
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History after getTask 1: " + history.get(i));
        }
        System.out.println(manager.getTask(2));
        System.out.println(manager.getEpic(3));
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History after getTask 2, getEpic 3: " + history.get(i));
        }
        System.out.println(manager.getSubTask(4));
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History after getSubTask: " + history.get(i));
        }
        System.out.println(manager.getSubTask(5));
        System.out.println(manager.getSubTask(6));
        System.out.println(manager.getEpic(7));
        System.out.println(manager.getTask(2));
        System.out.println(manager.getSubTask(4));
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History (check unique 2, 4): " + history.get(i));
        }*/
        /*Change statuses*/
        /*task = manager.getTask(1);
        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);
        subTask = manager.getSubTask(4);
        subTask.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(subTask);*/
        /*Check status changed */
        /*System.out.println("---Some statuses were changed");
        System.out.println(manager.getTask(1));
        System.out.println(manager.getSubTask(4));
        System.out.println(manager.getEpic(3));*/
        /*Delete task*/
        /*history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History (check before delete): " + history.get(i));
        }
        manager.deleteTask(2);
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History (check after delete, inner node 2): " + history.get(i));
        }*/
        /*Delete epic*/
        /*manager.deleteEpic(3);
        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println("History (check after delete, terminal node): " + history.get(i));
        }*/
        /*Print all*/
        /*System.out.println("---Some items were deleted");
        manager.printTasks();
        manager.printEpics();
        manager.printSubTasks();*/
    }
}
