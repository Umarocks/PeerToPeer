import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Peer {
    private static final int SERVER_PORT = 12345;
    private ServerSocket serverSocket;

    public Peer() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
    }

    public void startServer() {
        Thread serverThread = new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new FileRequestHandler(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    public void startClient() {
        Thread clientThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            try {
                System.out.print("Enter index server IP: ");
                String indexServerIp = scanner.nextLine();

                System.out.print("Enter your email: ");
                String email = scanner.nextLine();
                System.out.print("Enter your password: ");
                String password = scanner.nextLine();

                String fileServerInfo = authenticateWithIndexServer(indexServerIp, email, password);
                if (fileServerInfo == null) {
                    System.out.println("Authentication failed.");
                    return;
                }

                System.out.println("Connected to file server at: " + fileServerInfo);
                String[] fileServerParts = fileServerInfo.split(":");
                String fileServerHost = fileServerParts[0];
                int fileServerPort = Integer.parseInt(fileServerParts[1]);

                while (true) {
                    System.out.print("Enter file name to request, or 'exit' to quit: ");
                    String fileName = scanner.nextLine();
                    if (fileName.equalsIgnoreCase("exit")) {
                        break;
                    }
                    requestFile(fileServerHost, fileServerPort, fileName);
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            } finally {
                scanner.close();
                System.out.println("Client exited.");
            }
        });
        clientThread.start();
    }

    private String authenticateWithIndexServer(String indexServerIp, String email, String password) {
        try (Socket socket = new Socket(indexServerIp, 12346);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(email);
            out.println(password);
            return in.readLine();

        } catch (IOException e) {
            System.err.println("Failed to connect to index server: " + e.getMessage());
            return null;
        }
    }

    private void requestFile(String host, int port, String fileName) {
        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedInputStream fileIn = new BufferedInputStream(socket.getInputStream());
                FileOutputStream fileOut = new FileOutputStream("received_" + fileName)) {

            out.println(fileName);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }

            System.out.println("File " + fileName + " received.");
        } catch (IOException e) {
            System.err.println("File request failed: " + e.getMessage());
        }
    }

    private class FileRequestHandler implements Runnable {
        private final Socket socket;

        public FileRequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    OutputStream out = socket.getOutputStream()) {

                String fileName = in.readLine();
                File file = new File(fileName);
                if (file.exists()) {
                    byte[] buffer = new byte[4096];
                    try (FileInputStream fileIn = new FileInputStream(file)) {
                        int bytesRead;
                        while ((bytesRead = fileIn.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("File " + fileName + " sent.");
                } else {
                    System.out.println("File " + fileName + " not found.");
                }
            } catch (IOException e) {
                System.err.println("File handling error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close the socket: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Peer peer = new Peer();
            peer.startServer();
            peer.startClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
