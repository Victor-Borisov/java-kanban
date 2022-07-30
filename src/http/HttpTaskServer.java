package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.SubTask;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {

    private TaskManager taskManager;
    private HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) throws IOException {

        this.taskManager = taskManager;
        createHTTPServer();
    }

    private static String TASK = "task";
    private static String EPIC = "epic";
    private static String SUBTASK = "subtask";
    private static String HISTORY = "history";

    private final int PORT = 8080;
    private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public Gson getGson() {
        return gson;
    }

    private void createHTTPServer() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler());
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }
    public void startHttpServer() {
        httpServer.start();
    }
    public void stopHttpServer() {
        httpServer.stop(1);
    }

    private class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange h) throws IOException {
            String methodRequest = h.getRequestMethod();
            URI requestURI = h.getRequestURI();
            String path = requestURI.getPath();
            String[] splitPath = path.split("/");

            if (splitPath.length == 2 && methodRequest.equals("GET")) {
                handleGetPrioritizedTasks(h);
            }

            switch (methodRequest) {
                case "POST":
                    if (splitPath[2].equals(TASK)) {
                        handlePostAddUpdateTask(h);
                    } else if (splitPath[2].equals(EPIC)) {
                        handlePostAddUpdateEpic(h);
                    } else if (splitPath[2].equals(SUBTASK)) {
                        handlePostAddUpdateSubTask(h);
                    } else {
                        outputStreamWrite(h, "Запрашиваемая страница не найдена", 404);
                    }
                    break;
                case "GET":
                    if (splitPath[2].equals(TASK)) {
                        handleGetTaskGetTasksMap(h);
                    } else if (splitPath[2].equals(EPIC)) {
                        handleGetEpicGetEpicsMap(h);
                    } else if (splitPath[2].equals(SUBTASK)) {
                        handleGetSubTaskGetSubTasksMap(h);
                    } else if (splitPath[2].equals(HISTORY)) {
                        handleGetHistory(h);
                    } else {
                        outputStreamWrite(h, "Запрашиваемая страница не найдена", 404);
                    }
                    break;
                case "DELETE":
                    if (splitPath[2].equals(TASK)) {
                        handleDeleteTask(h);
                    } else if (splitPath[2].equals(EPIC)) {
                        handleDeleteEpic(h);
                    } else if (splitPath[2].equals(SUBTASK)) {
                        handleDeleteSubTask(h);
                    } else {
                        outputStreamWrite(h, "Запрашиваемая страница не найдена", 404);
                    }
                    break;
                default:
                    outputStreamWrite(h, "Неизвестный HTTP запрос", 405);
            }
        }

        int setId(HttpExchange httpExchange) {
            int id = Integer.parseInt(httpExchange.getRequestURI().toString()
                    .split("\\?")[1].split("=")[1]);
            return id;
        }

        void outputStreamWrite(HttpExchange h, String response, int code) throws IOException {
            h.sendResponseHeaders(code, 0);
            try (OutputStream os = h.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private String readText(HttpExchange h) throws IOException {
            return new String(h.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        }

        public void handleGetPrioritizedTasks(HttpExchange h) throws IOException {
            if (!taskManager.getPrioritizedTasks().isEmpty()) {
                outputStreamWrite(h, gson.toJson(taskManager.getPrioritizedTasks()), 200);
            } else {
                outputStreamWrite(h, "Отсортированный список задач не найден в базе.", 404);
            }
        }

        public void handlePostAddUpdateTask(HttpExchange h) throws IOException {
            String body = readText(h);
            if (body.isEmpty()) {
                outputStreamWrite(h, "Ничего не передано.", 400);
                return;
            }
            Task task = gson.fromJson(body, Task.class);
            Integer idTask = setId(h);
            if (idTask == null) {
                taskManager.createTask(task);
                outputStreamWrite(h, "Создали новую задачу с Id " + task.getId(), 200);
            } else {
                if (taskManager.getTasks().containsKey(idTask)) {
                    taskManager.updateTask(task);
                    outputStreamWrite(h, "Обновили задачу с Id "+ idTask, 200);
                } else {
                    outputStreamWrite(h, "Задачи с Id " + idTask + " нет в базе.", 404);
                }
            }
        }

        public void handlePostAddUpdateEpic(HttpExchange h) throws IOException {
            String body = readText(h);
            if (body.isEmpty()) {
                outputStreamWrite(h, "Ничего не передано.", 400);
                return;
            }
            Epic epic = gson.fromJson(body, Epic.class);
            Integer idEpic = setId(h);
            if (idEpic == null) {
                taskManager.createEpic(epic);
                outputStreamWrite(h, "Создали новый эпик с Id "+ epic.getId(), 200);
            } else {
                if (taskManager.getEpics().containsKey(idEpic)) {
                    taskManager.updateEpic(epic);
                    outputStreamWrite(h, "Обновили эпик с Id "+ idEpic, 200);
                } else {
                    outputStreamWrite(h, "Эпика с Id " + idEpic + " нет в базе.", 404);
                }
            }
        }

        public void handlePostAddUpdateSubTask(HttpExchange h) throws IOException {
            String body = readText(h);
            if (body.isEmpty()) {
                outputStreamWrite(h, "Ничего не передано.", 400);
                return;
            }
            SubTask subTask = gson.fromJson(body, SubTask.class);
            Integer idSubTask = setId(h);
            if (idSubTask == null) {
                if (taskManager.getEpics().containsKey(subTask.getEpicId())) {
                    taskManager.createSubTask(subTask);
                    outputStreamWrite(h, "Создали новую подзадачу с Id " + subTask.getId(), 200);
                } else {
                    outputStreamWrite(h, "Эпика с Id " + subTask.getEpicId() + " нет в базе.", 404);
                }
            } else {
                if (taskManager.getSubTasks().containsKey(idSubTask)) {
                    taskManager.updateSubTask(subTask);
                    outputStreamWrite(h, "Обновили подзадачу с Id "+ idSubTask, 200);
                } else {
                    outputStreamWrite(h, "Подзадачи с Id " + idSubTask + " нет в базе.", 404);
                }
            }
        }

        public void handleGetTaskGetTasksMap(HttpExchange h) throws IOException {
            if (h.getRequestURI().getQuery() != null) {
                int idTask = setId(h);
                if (taskManager.getTasks().containsKey(idTask)) {
                    Task task = taskManager.getTask(idTask);
                    outputStreamWrite(h, gson.toJson(task), 200);
                } else {
                    outputStreamWrite(h, "Задача с Id " + idTask + " не найдена в базе.", 404);
                }
            } else {
                if (!taskManager.getTasks().isEmpty()) {
                    outputStreamWrite(h, gson.toJson(taskManager.getTasks()), 200);
                } else {
                    outputStreamWrite(h, "Список задач не найден в базе.", 404);
                }
            }
        }

        public void handleGetEpicGetEpicsMap(HttpExchange h) throws IOException {
            if (h.getRequestURI().getQuery() != null) {
                int idEpic = setId(h);
                if (taskManager.getEpics().containsKey(idEpic)) {
                    Epic epic = taskManager.getEpic(idEpic);
                    outputStreamWrite(h, gson.toJson(epic), 200);
                } else {
                    outputStreamWrite(h, "Эпик с Id " + idEpic + " не найден в базе.", 404);
                }
            } else {
                if (!taskManager.getEpics().isEmpty()) {
                    outputStreamWrite(h, gson.toJson(taskManager.getEpics()), 200);
                } else {
                    String message = "Список эпиков не найден в базе.";
                    outputStreamWrite(h, message, 404);
                }
            }
        }

        public void handleGetSubTaskGetSubTasksMap(HttpExchange h) throws IOException {
            if (h.getRequestURI().getQuery() != null) {
                int idSubTask = setId(h);
                if (taskManager.getSubTasks().containsKey(idSubTask)) {
                    SubTask subTask = taskManager.getSubTask(idSubTask);
                    outputStreamWrite(h, gson.toJson(subTask), 200);
                } else {
                    outputStreamWrite(h, "Подзадача с Id " + idSubTask + " не найдена в базе.", 404);
                }
            } else {
                if (!taskManager.getSubTasks().isEmpty()) {
                    outputStreamWrite(h, gson.toJson(taskManager.getSubTasks()), 200);
                } else {
                    outputStreamWrite(h, "Список подзадач не найден в базе.", 404);
                }
            }
        }

        public void handleGetHistory(HttpExchange h) throws IOException {
            if (!taskManager.getHistory().isEmpty()) {
                outputStreamWrite(h, gson.toJson(taskManager.getHistory()), 200);
            } else {
                outputStreamWrite(h, "Cписок просмотра задач пуст.", 404);
            }
        }

        public void handleDeleteSubTask(HttpExchange h) throws IOException {
            if (h.getRequestURI().getQuery() != null) {
                int idSubTask = setId(h);
                if (taskManager.getSubTasks().containsKey(idSubTask)) {
                    SubTask subTask = taskManager.getSubTasks().get(idSubTask);
                    taskManager.deleteSubTask(subTask.getId());
                    outputStreamWrite(h, "Удалили " + gson.toJson(subTask), 200);
                } else {
                    outputStreamWrite(h, "Подзадача с Id " + idSubTask + " не найдена в базе.", 404);
                }
            } else {
                handleDeleteTasksEpicsSubTasksMap(h);
            }
        }

        public void handleDeleteEpic(HttpExchange h) throws IOException {
            if (h.getRequestURI().getQuery() != null) {
                int idEpic = setId(h);
                if (taskManager.getEpics().containsKey(idEpic)) {
                    Epic epic = taskManager.getEpics().get(idEpic);
                    taskManager.deleteEpic(epic.getId());
                    outputStreamWrite(h, "Удалили " + gson.toJson(epic), 200);
                } else {
                    outputStreamWrite(h, "Эпик с Id " + idEpic + " не найден в базе.", 404);
                }
            } else {
                handleDeleteTasksEpicsSubTasksMap(h);
            }
        }

        public void handleDeleteTask(HttpExchange h) throws IOException {
            if (h.getRequestURI().getQuery() != null) {
                int idTask = setId(h);
                if (taskManager.getTasks().containsKey(idTask)) {
                    Task task = taskManager.getTasks().get(idTask);
                    taskManager.deleteTask(task.getId());
                    outputStreamWrite(h, "Удалили " + gson.toJson(task), 200);
                } else {
                    outputStreamWrite(h, "Задача с Id " + idTask + " не найдена в базе.", 404);
                }
            } else {
                handleDeleteTasksEpicsSubTasksMap(h);
            }
        }

        public void handleDeleteTasksEpicsSubTasksMap(HttpExchange h) throws IOException {
            if (!taskManager.getTasks().isEmpty() ||
                    !taskManager.getEpics().isEmpty() ||
                    !taskManager.getSubTasks().isEmpty()) {
                taskManager.deleteAllTasks();
                taskManager.deleteAllSubTasks();
                taskManager.deleteAllEpics();
                outputStreamWrite(h, "Все задачи удалены.", 200);
            } else {
                outputStreamWrite(h, "Задач для удаления нет.", 404);
            }
        }
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy(HH:mm)");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.value(localDateTime.format(DATE_TIME_FORMATTER));
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), DATE_TIME_FORMATTER);
        }
    }
}