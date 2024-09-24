import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class IndexServer {
    private static final int INDEX_SERVER_PORT = 12346;
    private final Map<String, String> userDatabase = new HashMap<>(); // Example user database

    public IndexServer() {
        // Add some example users
        userDatabase.put("user1@example.com", "password1");
        userDatabase.put("user2@example.com", "password2");
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(INDEX_SERVER_PORT)) {
            System.out.println("Index Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new IndexRequestHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class IndexRequestHandler implements Runnable {
        private final Socket socket;

        public IndexRequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String email = in.readLine();
                String password = in.readLine();

                if (authenticate(email, password)) {
                    // Send back the IP and port for file requests
                    out.println("192.168.1.100:12345"); // Example IP and port of the peer
                } else {
                    out.println("AUTH_FAILED");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean authenticate(String email, String password) {
            return password.equals(userDatabase.get(email));
        }
    }

    public static void main(String[] args) {
        new IndexServer().start();
    }
}
