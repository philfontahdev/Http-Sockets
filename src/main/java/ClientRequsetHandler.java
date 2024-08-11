import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

public class ClientRequsetHandler implements Runnable {
    private final Socket clientSocket;

    public ClientRequsetHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

   public byte[] compressString(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
//        gzip.close();
        return obj.toByteArray();
    }
    @Override
    public void run() {
        // Since the tester restarts your program quite often, setting SO_REUSEADDR
        // ensures that we don't run into 'Address already in use' errors
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String requestLine = in.readLine();

            System.out.println("Received request: " + requestLine);

            if (requestLine.contains("GET")) {
                if (requestLine.contains("GET / HTTP/1.1")) {
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\n\r\n").getBytes());
                } else if (requestLine.contains("/echo")) {
                    in.readLine();
                    String header2 = in.readLine();
                    if (!header2.contains("Accept-Encoding:")) {
                        System.out.println("Received request: Echo" + Arrays.toString(requestLine.split(" ")));
                        String[] test = requestLine.split(" ");
                        String path = test[1];
                        System.out.println(Arrays.toString(path.split("/")));
                        String value = path.split("/")[2];
                        clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + value.length() + "\r\n\r\n" + value).getBytes());
                    }
//                        String ent = accept.split(",")[1];
                        System.out.println("Test Case: " + Arrays.toString(header2.split(" ")));
                        if(header2.contains("gzip")){
                            String[] test = requestLine.split(" ");
                            String path = test[1];
                            System.out.println(Arrays.toString(path.split("/")));
                            String value = path.split("/")[2];
                            System.out.println(Arrays.toString(compressString(value)));
                            clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + value.length() + "\r\nContent-Encoding: gzip\r\n\r\n").getBytes("UTF-8"));
                            clientSocket.getOutputStream().write(compressString(value));
                        }

                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n").getBytes());



                } else if (requestLine.contains("/user-agent")) {
                    in.readLine();
                    String useragent = in.readLine().split(" ")[1];
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + useragent.length() + "\r\n\r\n" + useragent).getBytes());
                } else if (requestLine.contains("/files")) {
                    String[] parts = requestLine.split(" ");
                    System.out.println(Arrays.toString(requestLine.split(" ")));
                    if (parts.length > 1) {
                        String filePath = parts[1].substring("/files/".length());
                        System.out.println(filePath);
                        Path fullPath = Paths.get(Main.directory, filePath);

                        if (Files.exists(fullPath)) {
                            byte[] fileContents = Files.readAllBytes(fullPath);
                            clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " + fileContents.length + "\r\n\r\n").getBytes());
                            clientSocket.getOutputStream().write(fileContents);
                        } else {
                            System.out.println("Received request: Error" + requestLine);
                            // Respond with a 404 Not Found status

                            clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                        }
                    }
                } else {
                    System.out.println("Received request: Error" + requestLine);
                    // Respond with a 404 Not Found status
                    clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }
            } else if (requestLine.contains("POST")) {
                if (requestLine.contains("/files")) {
                    String[] parts = requestLine.split(" ");
                    System.out.println(Arrays.toString(requestLine.split(" ")));
                    if (parts.length > 1) {
                        String filePath = parts[1].substring("/files/".length());
                        System.out.println(filePath);
                        Path fullPath = Paths.get(Main.directory);
                        try {
                            Files.createDirectories(fullPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        in.readLine();
                        in.readLine();
                        in.readLine();
                        in.readLine();
                        // Read body
                        StringBuffer bodyBuffer = new StringBuffer();
                        while (in.ready()) {
                            bodyBuffer.append((char) in.read());
                        }
                        String body = bodyBuffer.toString();
//                        in.readLine().split(" ")[1];
                        System.out.println(body);
                        Path file = fullPath.resolve(filePath);
                        try {
                            Files.write(file, body.getBytes(), StandardOpenOption.CREATE);
                            System.out.println("File created and content written successfully.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    clientSocket.getOutputStream().write("HTTP/1.1 201 Created\r\n\r\n".getBytes());
                    in.close();
                }
            } else {
                clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }


            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

}
