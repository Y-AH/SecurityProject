package security;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by yousef on 12/20/2016.
 */
public class TicketGrantingServer implements Runnable
{
    private static final String V_PASS[] = {"PPOPOOOP"};

    private final int TGS_PORT;
    private final String TGSKey = "P@ssw0rd123";
    private final int id;

    public TicketGrantingServer(int tgsPort, int id)
    {
        TGS_PORT = tgsPort;
        this.id = id;
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(TGS_PORT);
            while (true)
            {
                Socket client = serverSocket.accept();
                Thread handler = new Thread(() ->
                {
                    try
                    {
                        System.out.println("TGS: new connection received");
                        Scanner scanner = new Scanner(client.getInputStream());
                        String firstLine = scanner.nextLine();
                        PrintWriter clientWriter = new PrintWriter(client.getOutputStream(), true);

                        String args[] = firstLine.split(",");
                        if (args.length != 3)
                        {
                            clientWriter.println("TGS ERROR: expected 3 parameters to be passed " +
                                    "found + " + args.length + ".");
                            System.out.println("TGS ERROR: expected 3 parameters to be passed " +
                                    "found + " + args.length + ".");
                            clientWriter.close();
                            scanner.close();
                            client.close();
                            return;
                        }
                        int vID = Integer.parseInt(args[0]);
                        String ticketTGS = args[1];
                        String authenticator = args[2];
                        String decryptedTicket = CryptoUtils.Decrypt(ticketTGS, TGSKey);
                        String ticketTGSArgs[] = decryptedTicket.split(",");
                        if (ticketTGSArgs.length != 6)
                        {
                            clientWriter.println("TGS ERROR: Illegal Ticket.");
                            System.out.println("TGS ERROR: Illegal Ticket.");
                            clientWriter.close();
                            scanner.close();
                            client.close();
                            return;
                        }
                        String clientTGSKey = ticketTGSArgs[0];
                        int clientID = Integer.parseInt(ticketTGSArgs[1]);
                        String clientAddress = ticketTGSArgs[2];
                        int tgsID = Integer.parseInt(ticketTGSArgs[3]);
                        int timeStamp2 = Integer.parseInt(ticketTGSArgs[4]);
                        int lifeTime2 = Integer.parseInt(ticketTGSArgs[5]);


                        String authDecArgs[] = CryptoUtils.Decrypt(authenticator, clientTGSKey).split(",");
                        if (authDecArgs.length != 3)
                        {
                            clientWriter.println("TGS ERROR: Illegal Authenticator.");
                            System.out.println("TGS ERROR: Illegal Authenticator.");
                            clientWriter.close();
                            scanner.close();
                            client.close();
                            return;
                        }
                        int authClientID = Integer.parseInt(authDecArgs[0]);
                        String authClientAddress = authDecArgs[1];
                        int timeStamp3 = Integer.parseInt(authDecArgs[2]);
                        String clientRealAddress = ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress()
                                .toString().replace("/", "");
//                        if ((authClientID != clientID) || (!authClientAddress.equals(clientAddress)) ||
//                                (!authClientAddress.equals(clientRealAddress)) || (timeStamp2+1 != timeStamp3))
//                        {
//                            clientWriter.println("TGS ERROR: client identification failed");
//                            System.out.println("TGS ERROR: client identification failed");
//                            clientWriter.close();
//                            scanner.close();
//                            client.close();
//                            return;
//                        }
                        String vClientKey = CryptoUtils.GenerateKey();
                        String ticketV = vClientKey + "," + clientID + "," + clientAddress + "," + vID + "," +
                                (timeStamp3+1) + "," + "200";
                        String encryptTicketV = CryptoUtils.Encrypt(ticketV, V_PASS[vID]);
                        String msgToClient = vClientKey + "," + vID + "," + (timeStamp3+1) + "," + encryptTicketV;
                        String sendToClient = CryptoUtils.Encrypt(msgToClient, clientTGSKey);
                        clientWriter.println(sendToClient);
                        clientWriter.close();
                        scanner.close();
                        client.close();
                        System.out.println("TGS: finished processing client request.");
                    } catch (IOException ioException)
                    {
                        ioException.printStackTrace();
                    }
                }
                );
                handler.start();
            }
        } catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
}
