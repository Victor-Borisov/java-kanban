package model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Epic extends Task {
    private Map<Integer, SubTask> subTasksEpic = new HashMap<>() ;

    public Epic(String name, String description, LocalDateTime startTime, int duration) {
        super(name, description, Status.NEW, startTime, duration);
    }

    public Map<Integer, SubTask> getSubTasksEpic() {
        return subTasksEpic;
    }
    @Override
    public LocalDateTime getEndTime() {
        endTime = startTime;
        for (SubTask subTask : subTasksEpic.values()) {
            endTime = endTime.plusMinutes(subTask.getDuration());
        }
        return endTime;
    }
    public Type getType() { return Type.EPIC; }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", subTasksEpic=" + subTasksEpic +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(subTasksEpic, epic.subTasksEpic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasksEpic);
    }
}