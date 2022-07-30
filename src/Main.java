import http.HttpTaskServer;
import http.KVServer;
import service.HttpTaskManager;
import service.Managers;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Поехали!");
        KVServer kvServer = new KVServer();
        kvServer.start();
        new HttpTaskServer(
            new HttpTaskManager("http://localhost:8078", Managers.getDefaultHistory())
        ).startHttpServer();
    }
}
