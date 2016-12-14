package security;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by youse on 12/14/2016.
 */
public class Main
{
    public static void main(String args[]) throws Exception
    {
        AuthenticationServer authenticationServer = new AuthenticationServer(5555);
        Thread asThread = new Thread(authenticationServer);
        asThread.start();
        Socket socket = new Socket("localhost", 5555);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println("152,574,548\n");
    }
}
