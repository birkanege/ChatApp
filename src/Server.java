import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author birkanegee
 */
public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            ClientHandler clientThread = new ClientHandler(clientSocket, clients);
            clients.add(clientThread);
            new Thread(clientThread).start();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname; 
    private ClientHandler currentChatPartner; 

    public ClientHandler(Socket socket, List<ClientHandler> clients) throws IOException {
        this.clientSocket = socket;
        this.clients = clients;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public void run() {
        try {
            
            this.nickname = in.readLine().toLowerCase();
            onlineUserList();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("/request:")) {
                    handleChatRequest(inputLine);
                } else if (inputLine.startsWith("/accept:")) {
                    handleChatAcceptance(inputLine);
                } else if (inputLine.startsWith("/reject:")) {
                    handleChatRejection(inputLine);
                } else {
                    forwardMessage(inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("Error with client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void onlineUserList() {
        StringBuilder userList = new StringBuilder("ONLINE USERS:");
        for (ClientHandler client : clients) {
            userList.append(client.getNickname()).append(",");
        }
        for (ClientHandler client : clients) {
            client.out.println(userList.toString());
        }
    }

    private void handleChatRequest(String inputLine) {
        String[] parts = inputLine.split(":");
        String targetNickname = parts[1].toLowerCase();

        for (ClientHandler client : clients) {
            if (client.getNickname().equals(targetNickname)) {
                client.out.println("/request:" + nickname);
                return;
            }
        }
        out.println("User " + targetNickname + " is not online.");
    }

    private void handleChatAcceptance(String inputLine) {
        String[] parts = inputLine.split(":");
        String requester = parts[1].toLowerCase();

        for (ClientHandler client : clients) {
            if (client.getNickname().equals(requester)) {
               
                if (this.currentChatPartner != null) {
                    this.currentChatPartner.out.println(nickname + " has left the chat.");
                    this.currentChatPartner.currentChatPartner = null;
                }
                if (client.currentChatPartner != null) {
                    client.currentChatPartner.out.println(client.getNickname() + " has left the chat. Please send a request to chat.");
                    client.currentChatPartner.currentChatPartner = null;
                }

               
                this.currentChatPartner = client;
                client.currentChatPartner = this;

                client.out.println("/accepted:" + nickname);
                out.println("You are now chatting with " + client.getNickname());
                return;
            }
        }
    }

    private void handleChatRejection(String inputLine) {
        String[] parts = inputLine.split(":");
        String requester = parts[1].toLowerCase();

        for (ClientHandler client : clients) {
            if (client.getNickname().equals(requester)) {
                client.out.println("/rejected:" + nickname);
                return;
            }
        }
    }

    private void forwardMessage(String message) {
        if (currentChatPartner != null) {
            currentChatPartner.out.println(nickname + ": " + message);
        } else {
            out.println("No active chat. Please send a request to chat.");
        }
    }

    private void cleanup() {
        try {
            if (currentChatPartner != null) {
                currentChatPartner.out.println(nickname + " has left the chat. Please send a request to chat.");
                currentChatPartner.currentChatPartner = null;
            }

            clients.remove(this);
            onlineUserList();
            broadcastMessage("User " + nickname + " has left the chat.");

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.out.println(message);
        }
    }
    
}

