package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
     private static class Node {
        Task task;
        Node prev;
        Node next;
        public Node(Task task) {
            this.task = task;
            this.prev = null;
            this.next = null;
        }
    }
    private Node head;
    private Node tail;
    private final HashMap<Integer, Node> queueMap = new HashMap<>();

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
            if (node == head) {/*delete head*/
                if (head.next == null) {/*head is tail*/
                    head = null;
                    tail = null;
                    node = null;
                } else {
                    head = head.next;
                    head.prev = null;
                    node = null;
                }
            } else if (node == tail) {/*delete tail*/
                tail = tail.prev;
                tail.next = null;
                node = null;
            } else {/*delete middle node*/
                node.prev.next = node.next;
                node.next.prev = node.prev;
                node = null;
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
        Node currentNode = tail;
        while (currentNode != null) {
            tasks.add(currentNode.task);
            currentNode = currentNode.prev;
        }
        return tasks;
    }
}
