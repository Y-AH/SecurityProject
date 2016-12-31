package security;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Yousef on 12/29/2016.
 */
public class ServiceServer implements Runnable
{
    private final int Port;
    private final int ServiceID;
    private final String myKey = "PPOPOOOP";

    public ServiceServer(int Port, int ServiceID)
    {
        this.Port = Port;
        this.ServiceID = ServiceID;
    }


    @Override
    public void run()
    {
        try
        {
            ServerSocket serviceServerSocket = new ServerSocket(Port);
            Socket client = serviceServerSocket.accept();
            Thread handler = new Thread(()->{
                try
                {
                    System.out.println("Service: new connection received");
                    Scanner scanner = new Scanner(client.getInputStream());
                    String request = scanner.nextLine();
                    String args[] = request.split(",");
                    if (args.length != 2)
                    {
                        System.out.println("Service: ERROR invalid request");
                        return;
                    }
                    String decryptedTicketArgs[] = CryptoUtils.Decrypt(args[0], myKey).split(",");
                    if(decryptedTicketArgs.length != 6)
                    {
                        System.out.println("Service: ERROR Invalid ticket.");
                        return;
                    }
                    String clientServerKey = decryptedTicketArgs[0];
                    int clientID = Integer.parseInt(decryptedTicketArgs[1]);
                    String clientAddress = decryptedTicketArgs[2];
                    int serviceID = Integer.parseInt(decryptedTicketArgs[3]);
                    int timeStamp4 = Integer.parseInt(decryptedTicketArgs[4]);
                    int lifetime4 = Integer.parseInt(decryptedTicketArgs[5]);

                    String decryptedAuthenticator = CryptoUtils.Decrypt(args[1], clientServerKey);

                    PrintWriter clientWriter = new PrintWriter(client.getOutputStream());
                    clientWriter.println(CryptoUtils.Encrypt(""+ (timeStamp4+1), clientServerKey));
                    clientWriter.flush();
                    clientWriter.close();
                    scanner.close();
                    client.close();
                    System.out.println("Service: Done authenticating the user");

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
            handler.start();
        }
        catch (IOException ioE)
        {
            ioE.printStackTrace();
        }
    }
}
