package server;

import request.Request;
import handlers.Handler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;
    private final Map<String, Map<String, Handler>> handlers;
    private final Handler ifNotFoundHandler = ((request, out) -> {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    public Server(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.get(method) == null) {
            handlers.put(method, new ConcurrentHashMap<>());
        }

        handlers.get(method).put(path, handler);
    }

    public void listen(int port) {

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> handlerOfConnections(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlerOfConnections(Socket socket) {
        try (
                socket;
                final var in = socket.getInputStream();
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            var request = Request.fromInputStream(in);
            var handlerMap = handlers.get(request.getMethod());
            if (handlerMap == null) {
                ifNotFoundHandler.handle(request, out);
                return;
            }

            var handler = handlerMap.get(request.getPath());
            if (handler == null) {
                ifNotFoundHandler.handle(request, out);
                return;
            }

            handler.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
