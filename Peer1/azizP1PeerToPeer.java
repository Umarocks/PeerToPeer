import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.net.ServerSocket;
// 10.0.0.239

// 10.91.80.240
public class azizP1PeerToPeer {
    private static final int INDEX_SERVER_PORT = 10655; // Base port number from IndexServer

    public static void main(String[] args) {
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String indexServerIP = getIndexServerIP(userInput);
            int indexServerPort = getIndexServerPort(userInput);

            // Connect to the IndexServer
            Socket socket = new Socket(indexServerIP, indexServerPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = authenticateUser(userInput, out, in);
            System.out.println("Response: " + response);
            if (response.equals("FAILURE")) {
                System.out.println("Authentication failed.");
            } else {
                int assignedPort = Integer.parseInt(in.readLine());
                System.out.println("Assigned port: " + assignedPort);

                // Start a server socket on the assigned port
                new Thread(() -> startPeerServer(assignedPort)).start();

                loadFiles(socket, assignedPort, out);
                String loadResponse = in.readLine();
                if (!"FILES_ADDED".equals(loadResponse)) {
                    throw new IOException("Failed to load files on the server.");
                } // Handle content search and download
                while (true) {
                    System.out.println("Enter content name to search or 'exit' to quit:");
                    String contentName = userInput.readLine();
                    if (contentName.equalsIgnoreCase("exit")) {
                        System.out.println("Exiting...");
                        break;
                    }
                    System.out.println("SEARCH " + contentName);
                    String searchQuery = "SEARCH " + contentName;
                    out.println(searchQuery);
                    String contentResponse = in.readLine();
                    if (contentResponse.equals("NOT_FOUND")) {
                        System.out.println("Content not found.");
                    } else {
                        System.out.println("Content found at: " + contentResponse.split(" ")[1]);

                        // downloadContent(contentResponse.split(" ")[1], contentName);
                    }
                    System.out.println(
                            "Enter content name to download + Ip (resume.pdf 192.134.20.20:14415) or 'return' to go back to search:");
                    String downloadName = userInput.readLine();
                    if (downloadName.equalsIgnoreCase("return")) {
                        continue;
                    } else {
                        downloadContent(downloadName.split(" ")[1], downloadName.split(" ")[0]);
                    }

                }
            }

            // Close the connection
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
        return;
    }

    private static String getIndexServerIP(BufferedReader userInput) throws IOException {
        System.out.println("Enter IndexServer IP:");
        return userInput.readLine();
    }

    private static int getIndexServerPort(BufferedReader userInput) throws IOException {
        System.out.println("Enter IndexServer Port:");
        return Integer.parseInt(userInput.readLine());
    }

    private static String authenticateUser(BufferedReader userInput, PrintWriter out, BufferedReader in)
            throws IOException {
        while (true) {
            System.out.println("Choose an option: 1. Login 2. Register");
            String option = userInput.readLine();

            if (option.equals("1")) {
                loginUser(userInput, out, in);
                String response = in.readLine();
                if ("AUTH_SUCCESS".equals(response)) {
                    System.out.println("Login successful.");
                    return "SUCCESS";
                } else {
                    System.out.println("Login failed.");
                    return "FAILURE";
                }
            } else if (option.equals("2")) {
                registerUser(userInput, out, in);
                String response = in.readLine();
                if ("REGISTER_SUCCESS".equals(response)) {
                    System.out.println("Registration successful.");
                } else {
                    System.out.println("Registration failed.");
                    return "FAILURE";
                }
            } else {
                System.out.println("Invalid option. Exiting...");
                System.exit(0);
            }
        }
    }

    private static void loginUser(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Enter email:");
        String email = userInput.readLine();
        System.out.println("Enter password:");
        String password = userInput.readLine();
        out.println("LOGIN");
        out.println(email);
        out.println(password);
    }

    private static void registerUser(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Enter name:");
        String name = userInput.readLine();
        System.out.println("Enter phone:");
        String phone = userInput.readLine();
        System.out.println("Enter email:");
        String email = userInput.readLine();
        System.out.println("Enter username:");
        String username = userInput.readLine();
        System.out.println("Enter password:");
        String password = userInput.readLine();
        out.println("REGISTER");
        out.println(name);
        out.println(phone);
        out.println(email);
        out.println(username);
        out.println(password);
    }

    private static void startPeerServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Peer server is running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new FileRequestHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFiles(Socket socket, int assignedPort, PrintWriter out) throws IOException {

        try {
            File contentFolder = new File("ContentFile");
            HashMap<String, String> fileMap = new HashMap<>();
            if (contentFolder.exists() && contentFolder.isDirectory()) {
                for (File file : contentFolder.listFiles()) {

                    if (file.isFile()) {
                        fileMap.put(
                                file.getName(),
                                InetAddress.getLocalHost().getHostAddress() + ":" + assignedPort);

                    }
                }
            }

            // Convert the map to a CSV string
            StringBuilder csvBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : fileMap.entrySet()) {
                csvBuilder.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            }
            String csv = csvBuilder.toString();
            System.out.println(csv);
            out.println("LOADFILE");
            out.println(csv);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class FileRequestHandler implements Runnable {
        private final Socket socket;

        public FileRequestHandler(Socket socket) {
            this.socket = socket;
            System.err.println("FileRequestHandler created" + socket);
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String fileName = in.readLine();
                System.out.println("File requested: PeerServer " + fileName);
                File file = new File("./ContentFile/" + fileName);

                if (file.exists() && !file.isDirectory()) {
                    out.println("FILE_FOUND");
                    sendFile(file, socket);
                } else {
                    out.println("FILE_NOT_FOUND");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFile(File file, Socket socket) {
            try (FileInputStream fis = new FileInputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void downloadContent(String peerInfo, String fileName) {
        String[] parts = peerInfo.split(":");
        String peerIP = parts[0];
        int peerPort = Integer.parseInt(parts[1]);
        System.out.println("Downloading file from peer: " + peerIP + ":" + peerPort);
        try (Socket peerSocket = new Socket(peerIP, peerPort);
                PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
                BufferedInputStream bis = new BufferedInputStream(peerSocket.getInputStream())) {

            out.println(fileName);

            String response = new BufferedReader(new InputStreamReader(peerSocket.getInputStream())).readLine();
            if (response.equals("FILE_FOUND")) {
                File receiveDir = new File("receivefiles");
                if (!receiveDir.exists()) {
                    receiveDir.mkdir();
                }
                File file = new File(receiveDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    bos.flush();
                    System.out.println("File downloaded successfully.");
                }
            } else {
                System.out.println("File not found on peer.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}