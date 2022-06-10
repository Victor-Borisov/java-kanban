package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
     static class Node {
        Task task;
        Node prev;
        Node next;
        public Node(Task task) {
            this.task = task;
        }
    }
    private Node head;
    private Node tail;
    private HashMap<Integer, Node> queueMap = new HashMap<>();

    private void linkLast(Task task) {
        final Node newNode = new Node(task);
        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        queueMap.put(task.getId(), newNode);
    }
    private void removeNode(Node node) {
        if (node != null) {
            if (node == head) {
                head = node.next;
                tail = node.next;
            } else if (node == tail) {
                tail = node.prev;
                tail.next = null;
            } else {
                node.prev.next = node.next;
            }
        }
    }
    @Override
    public void remove(int id) {
        Node nodeToRemove = queueMap.remove(id);
        removeNode(nodeToRemove);
    }
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        final ArrayList<Task> tasks = new ArrayList<>();
        Node currentNode = head;
        while (currentNode != null) {
            tasks.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return tasks;
    }
}
