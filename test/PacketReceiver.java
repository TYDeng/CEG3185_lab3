import java.net.*;

import java.io.*;

public class PacketReceiver extends Thread {


    private static ServerSocket serverSocket;
    private static Socket Socket;
    private static DataInputStream in;


    public PacketReceiver(int port){
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("\nServer established, waiting for the client");

            //Waits for client connect
            Socket = serverSocket.accept();
            System.out.println("\n---[Client Connected]---\n");

            //Read from client
            in = new DataInputStream(new BufferedInputStream(Socket.getInputStream()));
            String data = in.readUTF();

            //Remove padding zeros
            data = removePad(data);

            System.out.println("IP Datagram (packet) received : \n"+ data.toUpperCase()+"\n");

            decode(data);

        } catch (Exception err){
            System.out.println(err);
        }
    }

    private static String removePad(String str){
        String[] strArr = str.split(" ");
        int len = Integer.parseInt(strArr[1],16);

        return (str.substring(0,(len*2)+(len/2)));
        
    }

    //Decodes the hex stream
    public static void decode(String in){
        String message="";
        String[] hex = in.split(" "); 
        //example 
        //4500 0028 1c46 4000 4006 9d35 c0a8 0003 c0a8 0001 
        //434f 4c4f 4d42 4941 2032 202d 204d 4553 5349 2030

        String header = hex[0];
        String len = hex[1];
        String idField = hex[2];
        String flags= hex[3];
        String tcp = hex[4];
        String csum = hex[5];
        String ipS = hex[6]+" "+hex[7];
        String ipD = hex[8]+" "+hex[9];

        for (int i=10; i<hex.length;i++){
            message+=hex[i];
        }

        boolean valid = checksum(header, len, idField, flags, tcp, csum, ipS, ipD);

        if (!valid){
            System.out.println("The verification of the checksum: Incorrect, Packet discarded!");
        } else{
            String sourceIP = getAdd(ipS);

            //28->00101000
            int lenPacket = Integer.parseInt(len.substring(2,4),16); 

            //Add 20 bytes to represents the payLoad size 
            int payLoad = Integer.parseInt(len.substring(0,2),16)+20; 

            message = convertToText(message);

            //Output required messages
            System.out.println("Received the data stream:");
            System.out.println("The data received from "+sourceIP+" is:");
            System.out.println("\n"+message);
            System.out.println("The data has "+(8*payLoad)+" bites or "+payLoad+" bytes. Total length of the packet is "+lenPacket+" bytes.");
            System.out.println("\nVerification of the checksum: Correct.\n");

        }
    }
    
    public static boolean checksum(String header, String len, String idField, String flags, String tcp, String checksum, String ipS, String ipD){
        
        String p1 = ipS.substring(0, 4);
        String p2 = ipS.substring(5);
        
        String p3 = ipD.substring(0, 4);
        String p4 = ipD.substring(5);

        //transform all the hexidecimals(base 16) to decimals (base 10)
        int headerDeco = Integer.parseInt(header,16);
        int lDecode = Integer.parseInt(len,16);
        int iFDecode = Integer.parseInt(idField,16);
        int fDecode = Integer.parseInt(flags,16);
        int tDecode= Integer.parseInt(tcp,16);
        int csumDecode =Integer.parseInt(checksum,16);
        
        int p1Decode = Integer.parseInt(p1,16);
        int p2Decode =Integer.parseInt(p2,16);
        int p3Decode = Integer.parseInt(p3, 16);
        int p4Decode = Integer.parseInt(p4,16);


        int sum = headerDeco+lDecode+iFDecode+fDecode+tDecode+csumDecode+p1Decode+p2Decode+p3Decode+p4Decode;
        
        //Perform addition to retrieve sum of all bits 
        String hexSum = Integer.toHexString(sum); //example transforms to 2FFFFD

        //Remove carry value and add to sum
        if (hexSum.length()>4){
            String carry= hexSum.substring(0,1);
            hexSum = hexSum.substring(1);
            int carryInt = Integer.parseInt(carry,16);
            int hexSumInt = Integer.parseInt(hexSum,16);
            sum = carryInt + hexSumInt;
            hexSum = Integer.toHexString(sum);
        }
        // Since the one's complement of FFFF is zero, return turn (correct)
        if (hexSum.equals("ffff")){
            return true;
        }
        //Return false for error
        return false;

    }

    //Transform hexidecimal component to return the ip address
    public static String getAdd(String address){

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