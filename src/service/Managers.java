package service;

public class Managers {
    public static TaskManager getDefault(HistoryManager historyManager) {
        //return new InMemoryTaskManager(historyManager);
        return new HTTPTaskManager("http://localhost:8078", historyManager, null, null);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
