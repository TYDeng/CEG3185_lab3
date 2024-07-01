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


    private static ServerSocket myService;
    private static Socket serviceSocket;
    private static DataInputStream input;


    public PacketReceiver(int port){
        try{
            myService = new ServerSocket(port);
            System.out.println("Server is up and running");

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

            System.out.println("IP Datagram (packet) received : "+ data.toUpperCase()+"\n");

            decodeFunc(data);

        } catch (Exception err){
            System.out.println(err);
        }
    }

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
        int csumDecode =Integer.parseInt(csum,16);
        
        int p1Decode = Integer.parseInt(p1,16);
        int p2Decode =Integer.parseInt(p2,16);
        int p3Decode = Integer.parseInt(p3, 16);
        int p4Decode = Integer.parseInt(p4,16);


        int sum = hDecode+lDecode+iFDecode+fDecode+tDecode+csumDecode+p1Decode+p2Decode+p3Decode+p4Decode;//perform addition to retrieve sum of all bits

        String hexSum = Integer.toHexString(sum); //example transforms to 2FFFFD

        //remove the carry value and add it to the sum value
        if (hexSum.length()>4){
            String cry= hexSum.substring(0,1);
            hexSum = hexSum.substring(1);
            int cryD = Integer.parseInt(cry,16);
            int hexSumD = Integer.parseInt(hexSum,16);
            sum=cryD+hexSumD;
            hexSum=Integer.toHexString(sum);
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

        int one = Integer.parseInt(addy.substring(0,2),16);
        int sec =  Integer.parseInt(addy.substring(2,4),16);
        int three =  Integer.parseInt(addy.substring(5,7),16);
        int four =  Integer.parseInt(addy.substring(7),16);
        return (one+"."+sec+"."+three+"."+four);
    
    }

    //Iterate through the hex code of the message in pairs and parse it to a character
    public static String convertToText(String str){
        StringBuilder sb = new StringBuilder();

        
        for (int i=0; i<str.length();i+=2){
            String c = str.substring(i, i+2);
            sb.append((char) Integer.parseInt(c,16));
        }
        return sb.toString();
    }

    /*
     * Decodes the stream and prints it on the screen
     */
    public static void decodeFunc(String input){

        String[] inputArr = input.split(" "); //example 4500 0028 1c46 4000 4006 9d35 c0a8 0003 c0a8 0001 434f 4c4f 4d42 4941 2032 202d 204d 4553 5349 2030

        String header = inputArr[0];
        String len = inputArr[1];
        String idField = inputArr[2];
        String flags= inputArr[3];
        String tcp = inputArr[4];
        String csum = inputArr[5];
        String ipS = inputArr[6]+" "+inputArr[7];
        String ipD = inputArr[8]+" "+inputArr[9];


        String msg="";

        for (int i=10; i<inputArr.length;i++){
            msg+=inputArr[i];
            
        }


        boolean bool = calcChecksumFunc(header, len, idField, flags, tcp, csum, ipS, ipD);

        if (!bool){
            System.out.println("The verification of the checksum demonstrates that the packet received is corrupted. Packet discarded!");
        } else{
            String ipSource = getAddy(ipS);


            int lenPacket = Integer.parseInt(len.substring(2,4),16); //28->00101000



            int payL = Integer.parseInt(len.substring(0,2),16)+20; // add 20 bytes to represents the payload size (as indicated in the requirements) 

            String decMsg = convertToText(msg);


            //Execute outputs as requested from lab requirements
            System.out.println("Receives the data stream and prints to the screen the data received with the following message:");
            System.out.println("The data received from "+ipSource+" is "+decMsg);
            System.out.println("The data has "+(8*payL)+" bites or "+payL+" bytes. Total length of the packet is "+lenPacket+" bytes.");
            System.out.println("The verification of the checksum demonstrates that the packet received is correct.");


        }
    }


    public static void main(String[] args) throws IOException {
        PacketReceiver receiver =new PacketReceiver(4999);
        
        // Close sockets and input reader
        input.close();
        serviceSocket.close();
        myService.close();

        
    }
    
}