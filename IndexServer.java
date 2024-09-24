import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexServer {
    private static final int INDEX_SERVER_PORT = 10655; // Base port number
    private static volatile int clientCounter = 0; // Counter to keep track of the number of clients
    private final Map<String, String> userDatabase = new HashMap<>(); // Example user database
    private final Map<String, String> contentDatabase = new HashMap<>(); // Example content database
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10); // Thread pool with 10 threads

    public IndexServer() {
        // Add some example users
        userDatabase.put("anna", "a86H6T0c");
        userDatabase.put("barbara", "G6M7p8az");
        userDatabase.put("bathie", "Pd82bG57");
        userDatabase.put("kdohas", "jO79bNs1");
        userDatabase.put("eli", "uCh781fY");
        userDatabase.put("tarah", "Cfw61RqV");
        userDatabase.put("tiff", "Kuz07YLv");

        // Add some example content
        contentDatabase.put("Aladdin", "192.55.32.214:15250");
        contentDatabase.put("Ten Years", "193.68.12.33:11985");
        contentDatabase.put("Stubborn", "152.64.92.45:12577");
        contentDatabase.put("Rock It", "193.68.12.33:11985");
        contentDatabase.put("Serenity", "192.55.32.214:15250");
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(INDEX_SERVER_PORT)) {
            System.out.println("Index Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new IndexRequestHandler(clientSocket));
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
                    int assignedPort;
                    synchronized (IndexServer.class) {
                        assignedPort = INDEX_SERVER_PORT + 1 + clientCounter; // Calculate the port number
                        clientCounter++; // Increment the counter for the next client
                    }

                    // Send back the assigned port for the peer
                    out.println(assignedPort);

                    // Handle content search requests
                    String contentRequest;
                    while ((contentRequest = in.readLine()) != null) {
                        if (contentDatabase.containsKey(contentRequest)) {
                            out.println(contentDatabase.get(contentRequest));
                        } else {
                            out.println("CONTENT_NOT_FOUND");
                        }
                    }
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
        IndexServer server = new IndexServer();
        server.start();
    }
}