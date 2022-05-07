import java.util.HashMap;
import java.util.Objects;

public class Epic extends Task {
    private HashMap<Integer, SubTask> subTasksEpic = new HashMap<>() ;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public HashMap<Integer, SubTask> getSubTasksEpic() {
        return subTasksEpic;
    }

    public void setSubTasksEpic(HashMap<Integer, SubTask> subTasksEpic) {
        if (subTasksEpic == null) {
            this.subTasksEpic.clear();
        } else {
            this.subTasksEpic = subTasksEpic;
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
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