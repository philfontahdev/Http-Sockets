import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static String directory;
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        // Parse command-line arguments to get the directory
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--directory") && i + 1 < args.length) {
                directory = args[i + 1];
                break;
            }
        }
//        // Check if the directory is set
//        if (directory == null) {
//            System.err.println("Directory not specified. Use --directory <path> to specify the directory.");
//            return;
//        }

        // Uncomment this block to pass the first stage

        try( ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);
            // Create a thread pool to handle multiple connections

            ExecutorService executorService = Executors.newCachedThreadPool();
            while (true) {

                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                executorService.execute(new ClientRequsetHandler(clientSocket));
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
