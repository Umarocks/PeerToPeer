
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

// 10.91.80.240
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
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String command;

                while (true) {
                    command = in.readLine();
                    String input = command.split(" ")[0];
                    try {
                        if (input == null) {
                            // Client has disconnected
                            System.out.println("Client disconnected: " + socket.getInetAddress());
                            break;
                        }
                        System.out.println("Command processing: " + command);

                        switch (input) {
                            case "REGISTER":
                                System.out.println("Register request received from " + socket.getInetAddress());
                                registerUser(in, out);
                                break;
                            case "LOGIN":
                                System.out.println("Login request received from " + socket.getInetAddress());
                                loginUser(in, out);
                                break;
                            case "LOADFILE":
                                System.out.println("Load file request received from " + socket.getInetAddress());
                                loadFiles(in, out);

                                break;
                            case "SEARCH":
                                System.out.println("Search request received from " + socket.getInetAddress());
                                searchContent(command, out);
                                break;
                            default:
                                System.out.println("Unknown command received: " + command);
                                continue;
                        }
                        System.out.println("Command processed: " + command);
                        socket.getOutputStream().flush();
                    } catch (IOException e) {

                        System.out.println("Error processing command for " + socket.getInetAddress() + " " + command);
                        e.printStackTrace();
                        break; // Exit the loop on error
                    }
                }

            } catch (IOException e) {
                System.out.println("Error processing command for " + socket.getInetAddress());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        private int loginUser(BufferedReader in, PrintWriter out) throws IOException {
            String username = in.readLine();
            String password = in.readLine();

            if (password.equals(userDatabase.get(username))) {
                int assignedPort = INDEX_SERVER_PORT + 1 + clientCounter; // Calculate the port number
                clientCounter++; // Increment the counter for the next client
                out.println("AUTH_SUCCESS");
                out.println(assignedPort);
                return 1;
            } else {
                out.println("AUTH_FAILURE");
                return 0;
            }
        }

        private void searchContent(String command, PrintWriter out) {
            String contentName = command.split(" ")[1];

            if (contentDatabase.containsKey(contentName)) {
                out.println("FOUND " + contentDatabase.get(contentName));
                System.out.println("Content found: " + contentName + " at " + contentDatabase.get(contentName));
            } else {
                out.println("NOT_FOUND");
            }
            out.flush();
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
            System.out.println("PROBLEMIS HERE 3");
            e.printStackTrace();
        }
    }

    private void loadFiles(BufferedReader in, PrintWriter out) {
        try {
            // Read the CSV string
            StringBuilder csvBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                csvBuilder.append(line).append("\n");
            }
            String csv = csvBuilder.toString();
            System.out.println("CSV: " + csv);
            // Convert the CSV string back to a Map
            Map<String, String> fileNameMap = new HashMap<>();
            String[] lines = csv.split("\n");
            System.out.println("Lines: " + lines.length);
            for (int i = 0; i < lines.length; i++) {
                String[] parts = lines[i].split(",");
                if (parts.length == 2) {
                    System.out.println("Parts: " + parts.length + " " + parts[0] + " " + parts[1]);
                    fileNameMap.put(parts[0], parts[1]);
                } else {
                    continue;
                }

            }

            // Process the map (e.g., add it to the content database)
            System.out.println("Received map: " + fileNameMap);
            // Add the received map to the content database
            contentDatabase.putAll(fileNameMap);
            // Append the received map to the fileList.txt
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("fileList.txt", true))) {
                for (Map.Entry<String, String> entry : fileNameMap.entrySet()) {
                    System.out.println("Writing to file: " + entry.getKey() + " " + entry.getValue());
                    writer.write(entry.getKey() + "," + entry.getValue());
                    writer.newLine();
                    writer.flush();

                }
                writer.close();
                out.println("FILES_ADDED");
            } catch (IOException e) {
                System.out.println("Error writing to fileList.txt");
                e.printStackTrace();
            }
            System.out.println("Files added to the index server");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;

    }

    public static void main(String[] args) {
        IndexServer server = new IndexServer();
        server.start();
    }
}