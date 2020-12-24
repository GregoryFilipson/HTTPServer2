package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> map;
    private final Map<String, String> query;
    private final InputStream in;

    private Request(String method, String path, Map<String, String> map,
                    InputStream in, Map<String, String> query) {
        this.method = method;
        this.path = path;
        this.map = map;
        this.in = in;
        this.query = query;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public InputStream getIn() {
        return in;
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            throw new IOException("Request is invalid");
        }

        var method = parts[0];
        var path = parts[1];

        String line;
        var headers = new HashMap<String, String>();
        while (true) {
            line = in.readLine();
            if (line.equals("")) {
                break;
            }
            var i = line.indexOf(":");
            var headerName = line.substring(0, i);
            var headerValue = line.substring(i + 2);
            headers.put(headerName, headerValue);
        }

        String body;

        while (method.equals("GET")) {
            final var contentLength = headers.get("Content-Length"); //считаю строку body
            final var length = contentLength.length(); //присваиваю длину в переменную
            final var bodyBytes = in.read(CharBuffer.allocate(length)); //считываю размер в байтах
            body = String.valueOf(bodyBytes); //считываю само body
            System.out.println(body);
        }

        var query = new HashMap<String, String>();
        String queryString;
        queryString = URLEncoder.encode(path, StandardCharsets.UTF_8);
        String[] params = queryString.split("&");
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            query.put(name, value);
        }

        return new Request(method, path, headers, inputStream, query);
    }

    @Override
    public String toString() {
        return "request.Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", map=" + map +
                '}';
    }
}
