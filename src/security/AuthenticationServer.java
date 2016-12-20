package security;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.Vector;

/**
 * Created by yousef on 12/14/2016.
 */
public class AuthenticationServer implements Runnable
{
    private static final String TGS_PASS[] = { "P@ssw0rd123"};
    private static final String CLIENTS_PASS[] = {"Faisal"};


    private final int serverPort;

    public AuthenticationServer(int serverPort)
    {
        this.serverPort = serverPort;
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
                System.out.println("AS: New Connection Accepted.");
                Thread handler = new Thread(() ->
                {

                    try
                    {
                        Scanner scanner = new Scanner(client.getInputStream());
                        PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                        String args[] = scanner.nextLine().split(",");
                        int clientID = Integer.parseInt(args[0]);
                        int ticketGrantingServerID = Integer.parseInt(args[1]);
                        int timeStamp1 = Integer.parseInt(args[2]);
                        SocketAddress socketAddress = client.getRemoteSocketAddress();
                        if (socketAddress instanceof InetSocketAddress)
                        {
                            InetSocketAddress iSocketAddress = (InetSocketAddress) socketAddress;
                            String clientAddress = iSocketAddress.getAddress().toString().replace("/", "");
                            System.out.println(
                                    String.format("AS: Client details:\n" +
                                            "\t- Client Address: %s\n" +
                                            "\t- Client ID: %d\n" +
                                            "\t- TGS ID: %d\n" +
                                            "\t- Time Stamp 1:%d",
                                            clientAddress,
                                            clientID,
                                            ticketGrantingServerID,
                                            timeStamp1)
                            );
                            String clientTicketGrantingServerKey = CryptoUtils.GenerateKey();
                            String ticketGrantingTicket = clientTicketGrantingServerKey + "," + clientID + ","
                                    + clientAddress + "," + ticketGrantingServerID + "," + (timeStamp1+1) +
                                    "," + "200";
                            String encTGT = CryptoUtils.Encrypt(ticketGrantingTicket, TGS_PASS[ticketGrantingServerID]);
                            String messageToClient = clientTicketGrantingServerKey + "," +
                                    ticketGrantingServerID + "," +
                                    (timeStamp1 + 1) + "," +
                                    "200" +  "," + encTGT;
                            String encMsg = CryptoUtils.Encrypt(messageToClient, CLIENTS_PASS[clientID]);
                            writer.println(encMsg);
                            writer.close();

                            System.out.println("AS: Finished Processing client request.");
                        }


                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
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
