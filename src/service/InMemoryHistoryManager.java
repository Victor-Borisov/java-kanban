package service;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> taskQueue = new LinkedList<>();
    @Override
    public void add(Task task) {
        if (task != null) {
            if (taskQueue.size() == 10) {
                taskQueue.remove();
            }
            taskQueue.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(taskQueue);
    }
}
