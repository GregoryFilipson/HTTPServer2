package handlers;

import java.io.BufferedOutputStream;
import request.Request;

@FunctionalInterface
public interface Handler {
    void handle(Request request, BufferedOutputStream out);
}
