import java.net.*;
import java.util.Random;
import java.io.*;
//Group 25
//Tengyang Deng 300156567
//Wenbo Yu 300161788


public class PacketSender extends Thread{

    private static Socket client;
    private static DataOutputStream dataoutput;

    // Constructor: sets up a socket connection and sends data to the specified address and port
    public PacketSender(String address, int port, String string){
        try{
            client = new Socket(address, port);
            System.out.println("IP Datagram (packet) to be sent: "+ string.toUpperCase());
            dataoutput = new DataOutputStream(client.getOutputStream());
            dataoutput.writeUTF(string);

        } catch (Exception e){
            System.out.println(e);

        }        
    }

    // Converts a string to its hexadecimal representation
    private static String constringhex (String string){
        StringBuffer read = new StringBuffer();
        char chararray[] = string.toCharArray();

        for (int i=0; i < chararray.length; i++){
            read.append(Integer.toHexString(chararray[i]));
        }
        return read.toString();
    }


    // Converts an IP address to its hexadecimal representation
    private static String coniphex (InetAddress ip){
        StringBuffer read = new StringBuffer();
        String string = (ip.toString()).replace("/", "");
        String[] tstring = string.split("\\.");

        for (int i =0; i < tstring.length; i++){
            String hex = Integer.toHexString(Integer.parseInt(tstring[i]));

            if (hex.length()!= 2){
                hex="0"+hex;
            }

            read.append(hex);
        }
        return read.toString();

    }
    

    // Splits a string into sections of four characters each for readability
    private static String devsting (String string){
        StringBuffer read = new StringBuffer();

        char chararray[] = string.toCharArray();

        for(int i = 0; i < chararray.length; i++) {
            read.append(chararray[i]);

            if(( i + 1 ) % 4 == 0) {
                read.append(" ");
            }

        }

        return read.toString();

    }


    // Calculates the length of a payload in hexadecimal with a fixed addition of 20 bytes for the header
    private static String getpayloadlen (String string){
        int l = string.length()+20;
        String hexpy = Integer.toHexString(l);

        if (hexpy.length() == 1){
            return "000" + hexpy;
        }
        else if (hexpy.length() == 2) {
            return "00" + hexpy;

        }
        else if (hexpy.length() == 3){
            return "0" + hexpy;

        }
        return hexpy;
    }

    // Calculates a checksum for the packet
    private static String calc(String string){
        
        string = devsting (string);
        String[] stringlist = string.split(" ");
        int count = 0;

        for (int i = 0; i < stringlist.length; i++){
            count +=  Integer.parseInt( stringlist[i], 16);
        }

        String sum = Integer.toHexString(count);

        if (sum.length() != 4){
            String first = sum.substring(0,1);
            sum = sum.substring(1);
            count = Integer.parseInt(sum,16)+Integer.parseInt(first,16);
        }
        count = 65535 - count;

        return Integer.toHexString(count);

    }

    // Pads the payload to ensure it fits a certain size
    private static String spy(String string){

        while (string.length() % 8 != 0){
            string = string + "0";
        }
        return string;
    }

    // Generates a random identification field for each packet
    private static String radidf(){

        Random rad = new Random();
        String id = Integer.toHexString(rad.nextInt(65535+1));

        
        if (id.length() == 1){
            System.out.println("Yes");
            return "000" + id;
        }
        else if(id.length() == 2){
            System.out.println("No");
            return "00" + id;
        }
        else if(id.length() == 3){
            System.out.println("~");
            return "0" + id;
        }

        return id;

    }

    // Encodes the payload and other necessary information into a packet
    private static String encode(InetAddress cip, InetAddress sip, String pl){

        String headerl = "45";
        String typeofs = "00";
        String fo ="4000"; 
        String ttltcp ="4006"; 
        String id = radidf() ;


        String clientip = coniphex(cip);
        String serverip = coniphex(sip);
        String payload = constringhex(pl);

        String plen = getpayloadlen(pl);
        String csum = calc(headerl + typeofs + plen + id + fo + ttltcp + clientip + serverip);

        System.out.println("Checksum value: "+ (csum.toUpperCase())+"\n" );

        String data = headerl + typeofs + plen + id + fo + ttltcp + csum + clientip + serverip + spy(payload);

        return devsting (data);
    }

    public static void main(String[] args) throws IOException {

        String destip =InetAddress.getLocalHost().getHostAddress();
        String sip = destip;
        String machineName = InetAddress.getLocalHost().getHostName(); 
        String payload = "COLOMBIA 2 - MESSI 0";
        
 

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        
        if(args.length!=4) {
            System.out.println("***********Welcome**************");
            System.out.println("Would you like to enter custom data? Type y for yes ");
            String option = input.readLine();
            if (option.toLowerCase().equals("y")){
                System.out.println("------------------------------------");
                System.out.println("\nEnter a Server/Destination IP:   ");
                destip = input.readLine();
                System.out.println("\nEnter Payload:   ");
                payload = input.readLine();
                System.out.println("------------------------------------");
            }
        }
        else if (args.length == 4){
            destip =args[1];
            payload = args[3];
        }

        System.out.println("-----Thank You!-----\n");
        System.out.println("\n----- Provided data ------");
        System.out.println("Source IP: "+ sip);
        System.out.println("Destination IP: "+ destip);
        System.out.println("Payload: "+ payload);
        System.out.println("------------------------\n");

        String data = encode(InetAddress.getByName(sip), InetAddress.getByName(destip), payload);

        new PacketSender(machineName, 4999, data);

        client.close();
        dataoutput.close();
        input.close();

    }
    
}
