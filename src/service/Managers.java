package service;

public class Managers {
    public static TaskManager getDefault(HistoryManager historyManager) {
        //return new InMemoryTaskManager(historyManager);
        return new HttpTaskManager("http://localhost:8078", historyManager, false );
    }

    public static TaskManager getInMemoryTaskManager(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
