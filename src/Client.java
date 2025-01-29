
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
/**
 *
 * @author birkanegee
 */
public class Client {
   private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;
    private String nickname;

    public Client(String serverAddress, int serverPort, Consumer<String> onMessageReceived, String nickname) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.onMessageReceived = onMessageReceived;
        this.nickname = nickname.toLowerCase();

       
        out.println(nickname);
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void sendChatRequest(String targetNickname) {
        out.println("/request:" + targetNickname);
    }

    public void acceptChat(String requesterNickname) {
        out.println("/accept:" + requesterNickname);
    }

    public void rejectChat(String requesterNickname) {
        out.println("/reject:" + requesterNickname);
    }

    public void startClient() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessageReceived.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void closeConnection() throws IOException {
        socket.close();
    }
}
