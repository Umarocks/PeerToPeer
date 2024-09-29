import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class IndexServer {
    private static final int INDEX_SERVER_PORT = 10655; // Base port number
    private static int clientCounter = 0; // Counter to keep track of the number of clients
    private final Map<String, String> userDatabase = new HashMap<>(); // Example user database
    private final Map<String, String> contentDatabase = new HashMap<>(); // Example content database

    public IndexServer() {
        // Load users from file
        loadUsersFromFile();

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

                String command = in.readLine();
                if (command.equals("REGISTER")) {
                    registerUser(in, out);
                } else if (command.equals("LOGIN")) {
                    loginUser(in, out);
                } else if (command.startsWith("SEARCH")) {
                    searchContent(command, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void registerUser(BufferedReader in, PrintWriter out) throws IOException {
            String name = in.readLine();
            String phone = in.readLine();
            String email = in.readLine();
            String username = in.readLine();
            String password = in.readLine();

            if (!userDatabase.containsKey(username)) {
                userDatabase.put(username, password);
                saveUserToFile(name, phone, email, username, password);
                out.println("REGISTER_SUCCESS");
            } else {
                out.println("REGISTER_FAILURE");
            }
        }

        private void loginUser(BufferedReader in, PrintWriter out) throws IOException {
            String username = in.readLine();
            String password = in.readLine();

            if (password.equals(userDatabase.get(username))) {
                int assignedPort = INDEX_SERVER_PORT + 1 + clientCounter; // Calculate the port number
                clientCounter++; // Increment the counter for the next client
                out.println("AUTH_SUCCESS");
                out.println(assignedPort);
            } else {
                out.println("AUTH_FAILURE");
            }
        }

        private void searchContent(String command, PrintWriter out) {
            String contentName = command.split(" ")[1];
            if (contentDatabase.containsKey(contentName)) {
                out.println("FOUND " + contentDatabase.get(contentName));
            } else {
                out.println("NOT_FOUND");
            }
        }

        private void saveUserToFile(String name, String phone, String email, String username, String password) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("userList.txt", true))) {
                writer.write(name + "," + phone + "," + email + "," + username + "," + password);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("userList.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String username = parts[3];
                    String password = parts[4];
                    userDatabase.put(username, password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        IndexServer server = new IndexServer();
        server.start();
    }
}