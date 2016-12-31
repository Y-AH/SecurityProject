package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        TicketGrantingServer ticketGrantingServer = new TicketGrantingServer(54454, 0);
        Thread tgsThread = new Thread(ticketGrantingServer);
        tgsThread.start();

        ServiceServer serviceServer = new ServiceServer(36864, 0);
        Thread serviceThread = new Thread(serviceServer);
        serviceThread.start();

        Client client = new Client(0, 0, 0, "Faisal");
        Thread clientThread = new Thread(client);
        clientThread.start();

    }
}
