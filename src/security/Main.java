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
        Socket ASSocket = new Socket("localhost", 5555);
        String myKey = "Faisal";
        PrintWriter writer = new PrintWriter(ASSocket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(ASSocket.getInputStream()));
        writer.println("0,0,0");
        String ASReply = "";
        while (!ASSocket.isClosed())
        {
            if (reader.ready())
            {
                ASReply = reader.readLine();
                System.out.println(ASReply);
                ASSocket.close();
            }
        }
        String plainTextReply = CryptoUtils.Decrypt(ASReply, myKey);
        System.out.println("Client: AS reply is:\n\t" + plainTextReply);

    }
}
