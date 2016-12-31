package security;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Yousef on 12/26/2016.
 */
public class Client implements Runnable
{
    private final int TicketGrantingServerID;
    private final int ServiceServerID;
    private final String ClientPassword;
    private final int ClientID;

    private final String AuthenticationServerHostname = "localhost";
    private final int AuthenticationServerPort = 5555;

    private final String[] TicketGrantingServersHostnames = {"localhost"};
    private final int[] TicketGrantingServersPorts = {54454};

    private final String[] ServiceServerSocketHostnames = {"localhost"};
    private final int[] ServiceServerPorts = {36864};

    public Client(int ClientID,int TicketGrantingServerID, int ServiceServerID, String ClientPassword)
    {
        this.TicketGrantingServerID = TicketGrantingServerID;
        this.ServiceServerID = ServiceServerID;
        this.ClientPassword = ClientPassword;
        this.ClientID = ClientID;
    }

    @Override
    public void run()
    {
        try
        {
            Socket AuthenticationServerSocket = new Socket(AuthenticationServerHostname, AuthenticationServerPort);
            PrintWriter AuthenticationWriter = new PrintWriter(AuthenticationServerSocket.getOutputStream());
            AuthenticationWriter.println(String.format("%d,%d,%d", ClientID, TicketGrantingServerID, 0));
            AuthenticationWriter.flush();
            Scanner AuthenticationScanner = new Scanner(AuthenticationServerSocket.getInputStream());
            String response = AuthenticationScanner.nextLine();
            String decryptedResponse = CryptoUtils.Decrypt(response, ClientPassword);
            String responseArgs[] = decryptedResponse.split(",");
            if (responseArgs.length != 5)
            {
                System.out.println("Client: ERROR: invalid response.");
                return;
            }
            String clientTGSKey = responseArgs[0];
            int tgsID = Integer.parseInt(responseArgs[1]);
            int timeStamp2 = Integer.parseInt(responseArgs[2]);
            int lifeTime2 = Integer.parseInt(responseArgs[3]);
            String tgsTicket = responseArgs[4];
            assert(tgsID == TicketGrantingServerID);
            AuthenticationWriter.close();
            AuthenticationScanner.close();
            AuthenticationServerSocket.close();


            System.out.println("Client: finished from AS");

            Socket TicketGrantingServerSocket = new Socket(TicketGrantingServersHostnames[TicketGrantingServerID],
                    TicketGrantingServersPorts[TicketGrantingServerID]);


            PrintWriter TicketGrantingServerWriter = new PrintWriter(TicketGrantingServerSocket.getOutputStream(),true);
            TicketGrantingServerWriter.println(String.format(
                    "%d,%s,%s", ServiceServerID, tgsTicket, CryptoUtils.Encrypt(String.format(
                            "%d,%s,%d", ClientID, "localhost", (timeStamp2+1)
                    ), clientTGSKey)
            ));
            TicketGrantingServerWriter.flush();
            Scanner tgsScanner = new Scanner(TicketGrantingServerSocket.getInputStream());
            String tgsResponse = tgsScanner.nextLine();
            tgsResponse = CryptoUtils.Decrypt(tgsResponse, clientTGSKey);
            String tgsArgs[] = tgsResponse.split(",");
            if (tgsArgs.length != 4)
            {
                System.out.println("Client: ERROR invalid tgs response");
                return;
            }
            String clientServerKey = tgsArgs[0];
            int serverID = Integer.parseInt(tgsArgs[1]);
            int timeStamp4 = Integer.parseInt(tgsArgs[2]);
            String serverTicket = tgsArgs[3];
            TicketGrantingServerWriter.close();
            tgsScanner.close();
            TicketGrantingServerSocket.close();


            Socket serverSocket = new Socket(ServiceServerSocketHostnames[ServiceServerID],
                    ServiceServerPorts[ServiceServerID]);
            PrintWriter ServiceServerWriter = new PrintWriter(serverSocket.getOutputStream(), true);
            String auth = CryptoUtils.Encrypt(
                                String.format("%d,%s,%d",ClientID, "localhost", (timeStamp4+1))
                                ,clientServerKey);
            ServiceServerWriter.println(
                    String.format("%s,%s", serverTicket, auth)
            );
            ServiceServerWriter.flush();
            Scanner ServiceServerScanner = new Scanner(serverSocket.getInputStream());
            String vResponse = ServiceServerScanner.nextLine();
            vResponse = CryptoUtils.Decrypt(vResponse, clientServerKey);
            String vArgs[] = vResponse.split(",");
            if (vArgs.length != 1)
            {
                System.out.println("Client: ERROR");
                return;
            }
            System.out.println("Client: Done authenticating.");

        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
}



































