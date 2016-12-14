package security;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by youse on 12/14/2016.
 */
public class AuthenticationServer implements Runnable
{
    private final int serverPort;
    private Vector<Socket> clients;

    public AuthenticationServer(int serverPort)
    {
        this.serverPort = serverPort;
        clients = new Vector<Socket>();
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true)
            {
                Socket client = serverSocket.accept();
                clients.add(client);
                Thread handler = new Thread(() ->
                {

                    try
                    {
                        Scanner scanner = new Scanner(client.getInputStream());
                        PrintWriter writer = new PrintWriter(client.getOutputStream());
                        String args[] = scanner.nextLine().split(",");
                        int clientID = Integer.parseInt(args[0]);
                        int ticketGrantingServerID = Integer.parseInt(args[1]);
                        int timeStamp1 = Integer.parseInt(args[2]);
                        String clientAddress = client.getRemoteSocketAddress().toString();
                        System.out.println(clientAddress);

                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    finally
                    {
                        clients.remove(client);
                    }
                });
                handler.start();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
