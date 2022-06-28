import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;
import service.Managers;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    void beforeEachTest() {
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
        super.beforeEachTest();
    }
}
