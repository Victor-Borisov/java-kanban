public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        Manager manager = new Manager();
        Task task;
        SubTask subTask;
        Epic epic;
        /*Create tasks*/
        manager.createTask("Покупка билетов", "Купить билеты");
        manager.createTask("Бронирование жилья", "Арендовать квартиру");
        manager.createEpic("Релокация", "Переехать жить и работать в другую страну");
        manager.createSubTask(3, "Подготовка документов", "Подготовить все документы");
        manager.createSubTask(3,"Устройство на работу", "Устроиться на работу в новой локации");
        manager.createSubTask(3, "Открытие счёта", "Открыть счёт в банке");
        manager.createEpic("Получение гражданства", "Получить гражданство");
        manager.createSubTask(7, "Получение гражданства", "Получить гражданство");
        /*Print tasks*/
        System.out.println(manager.getTask(1));
        System.out.println(manager.getTask(2));
        System.out.println(manager.getEpic(3));
        System.out.println(manager.getSubTask(4));
        System.out.println(manager.getSubTask(5));
        System.out.println(manager.getSubTask(6));
        System.out.println(manager.getEpic(7));
        System.out.println(manager.getSubTask(8));
        /*Change statuses*/
        task = manager.getTask(1);
        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);
        subTask = manager.getSubTask(4);
        subTask.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(subTask);
        /*Check status changed */
        System.out.println("---Some statuses were changed");
        System.out.println(manager.getTask(1));
        System.out.println(manager.getSubTask(4));
        System.out.println(manager.getEpic(3));
        /*Delete task*/
        manager.deleteTask(2);
        /*Delete epic*/
        manager.deleteEpic(3);
        /*Delete subtask*/
        manager.deleteSubTask(8);
        /*Print all*/
        System.out.println("---Some items were deleted");
        manager.printTasks();
        manager.printEpics();
        manager.printSubTasks();
    }
}
