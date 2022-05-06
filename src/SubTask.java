import java.util.Objects;

public class SubTask extends Task {
    private int linkToEpic;

    public SubTask(String name, String description, int id, Enum status, int linkToEpic) {
        super(name, description, id, status);
        this.linkToEpic = linkToEpic;
    }

    public int getLinkToEpic() {
        return linkToEpic;
    }

    public void setLinkToEpic(int linkToEpic) {
        this.linkToEpic = linkToEpic;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", linkToEpic=" + linkToEpic +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        SubTask subTask = (SubTask) object;
        return linkToEpic == subTask.linkToEpic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), linkToEpic);
    }
}