import http.HttpTaskServer;
import http.KVServer;
import service.HTTPTaskManager;
import service.Managers;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Поехали!");
        KVServer kvServer = new KVServer();
        kvServer.start();
        new HttpTaskServer(
            new HTTPTaskManager("http://localhost:8078", Managers.getDefaultHistory(), null, null)
        ).startHttpServer();
    }
}
