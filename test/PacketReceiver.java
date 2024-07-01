import java.net.*;
import java.io.*;

    /**
     * @class CEG 3185 Lab 3
     * @author Michias Shiferaw and Teodora Vukojevic
     * @since June 22 2023
     * @version 2.1
     * @param args
     */

public class PacketReceiver extends Thread {


    private static ServerSocket serverSocket;
    private static Socket Socket;
    private static DataInputStream in;

    // Constructor initializes a server socket, accepts a client connection, and processes received data.
    public PacketReceiver(int port){
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("\nServer established, waiting for the client");

            //Creating socket and waiting for client connection
            serviceSocket = myService.accept();

            //Establish socket connection to server
            System.out.println("\n***__Client Connected __***\n");

            //Read from client socket to DataInputStream Object
            input = new DataInputStream(new BufferedInputStream(serviceSocket.getInputStream()));

            //Reads the DataInputStream obj as a string that has been encoded using UTF-8 format
            String data = input.readUTF();

            //Remove the added padding zeros
            data = removePad(data);

            System.out.println("IP Datagram (packet) received : \n"+ data.toUpperCase()+"\n");

            decode(data);

        } catch (Exception err){
            System.out.println(err);
        }
    }

    // Removes padding from received data based on specified length.
    private static String removePad(String str){
        String[] strArr = str.split(" ");
        int len = Integer.parseInt(strArr[1],16);

        return (str.substring(0,(len*2)+(len/2)));
        
    }

    public static boolean calcChecksumFunc(String header, String len, String idField, String flags, String tcp, String csum, String ipS, String ipD){
        
        String p1 = ipS.substring(0, 4);
        String p2 = ipS.substring(5);
        
        String p3 = ipD.substring(0, 4);
        String p4 = ipD.substring(5);

        //transform all the hexidecimals(base 16) to decimals (base 10)
        int hDecode = Integer.parseInt(header,16);
        int lDecode = Integer.parseInt(len,16);
        int iFDecode = Integer.parseInt(idField,16);
        int fDecode = Integer.parseInt(flags,16);
        int tDecode= Integer.parseInt(tcp,16);
        int csumDecode =Integer.parseInt(checksum,16);
        
        int p1Decode = Integer.parseInt(p1,16);
        int p2Decode =Integer.parseInt(p2,16);
        int p3Decode = Integer.parseInt(p3, 16);
        int p4Decode = Integer.parseInt(p4,16);


        int sum = hDecode+lDecode+iFDecode+fDecode+tDecode+csumDecode+p1Decode+p2Decode+p3Decode+p4Decode;//perform addition to retrieve sum of all bits

        String hexSum = Integer.toHexString(sum); //example transforms to 2FFFFD

        //remove the carry value and add it to the sum value
        if (hexSum.length()>4){
            String carry= hexSum.substring(0,1);
            hexSum = hexSum.substring(1);
            int carryInt = Integer.parseInt(carry,16);
            int hexSumInt = Integer.parseInt(hexSum,16);
            sum = carryInt + hexSumInt;
            hexSum = Integer.toHexString(sum);
        }
        // if the value is FFFF then its one's complement is zero (no error)
        if (hexSum.equals("ffff")){
            return true;
        }
        //otherwise return false to throw error msg
        return false;

    }

    //Transform hexidecimal component to return the param's ip address
    public static String getAddy(String addy){

        int one = Integer.parseInt(address.substring(0,2),16);
        int sec =  Integer.parseInt(address.substring(2,4),16);
        int three =  Integer.parseInt(address.substring(5,7),16);
        int four =  Integer.parseInt(address.substring(7),16);
        return (one+"."+sec+"."+three+"."+four);
    
    }

    //Iterate through the hex code of the message in pairs
    public static String convertToText(String str){
        StringBuilder pairs = new StringBuilder();

        for (int i=0; i<str.length();i+=2){
            String c = str.substring(i, i+2);
            pairs.append((char) Integer.parseInt(c,16));
        }
        return pairs.toString();
    }

    public static void main(String[] args) throws IOException {
        PacketReceiver receiver = new PacketReceiver(4999);
        
        //close socket
        in.close();
        Socket.close();
        serverSocket.close();
    }
    
}
