import java.net.*;
import java.io.*;

public class PacketSender {
    public static void main(String[] args) throws Exception{

        //try to connect to server - localhost @ port 8888
        Socket client = new Socket("localhost",8888);
        //if server is not listening - You will get Exception
        // java.net.ConnectException: Connection refused: connect

        //write to server using output stream
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        out.writeUTF("Hello server - How are you sent by Client");

        //read from the server
        DataInputStream in = new DataInputStream(client.getInputStream());
        System.out.println("Data received from the server is -> " + in.readUTF());

        //close the connection
        client.close();
    }
}