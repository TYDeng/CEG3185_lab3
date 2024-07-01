import java.net.*;
import java.util.Random;
import java.io.*;
//Group 25
//Tengyang Deng 300156567
//Wenbo Yu 300161788


public class PacketSender extends Thread{

    private static Socket myClient;
    private static DataOutputStream output;

    // Constructor: sets up a socket connection and sends data to the specified address and port
    public PacketSender(String address, int port, String data){
        try{
            myClient = new Socket(address, port);

            System.out.println("IP Datagram (packet) to be sent: "+ data.toUpperCase());

            output = new DataOutputStream(myClient.getOutputStream());
            output.writeUTF(data);

        } catch (Exception err){
            System.out.println(err);

        }        
    }

    // Converts a string to its hexadecimal representation
    private static String convertStrToHex(String str){
        
        StringBuffer sb = new StringBuffer();

        char ch[] = str.toCharArray();

        for (int i=0;i<ch.length;i++){
            sb.append(Integer.toHexString(ch[i]));
        }
        return sb.toString();

    }


    // Converts an IP address to its hexadecimal representation
    private static String convertIPToHex(InetAddress address){
        
        StringBuffer sb = new StringBuffer();

        String str = (address.toString()).replace("/", "");

        String[] words = str.split("\\.");

        for (int i =0;i<words.length;i++){
            String hexString = Integer.toHexString(Integer.parseInt(words[i]));

            if (hexString.length()!=2){
                hexString="0"+hexString;
            }
            sb.append(hexString);
        }

        return sb.toString();

    }
    

    // Splits a string into sections of four characters each for readability
    private static String splitFunc(String str){
        StringBuffer sb = new StringBuffer();

        char ch[] = str.toCharArray();

        for(int i = 0; i < ch.length; i++) {
            sb.append(ch[i]);
            if((i+1)%4==0) {
                sb.append(" ");
            }
        }

        return sb.toString();

    }


    // Calculates the length of a payload in hexadecimal with a fixed addition of 20 bytes for the header
    private static String getLength(String str){
        int len = str.length()+20;

        String lenHex = Integer.toHexString(len);

        if (lenHex.length()==1){
            return "000"+lenHex;

        } else if (lenHex.length()==2){
            return "00"+lenHex;

        }
        else if (lenHex.length()==3){
            return "0"+lenHex;

        }

        return lenHex;
    }

    // Calculates a checksum for the packet
    private static String calcChecks(String s){
        
        s = splitFunc(s);
        String[] words = s.split(" ");

        int cint = 0;

        
        for (int i=0; i<words.length;i++){
            cint+=  Integer.parseInt(words[i], 16);
        }

        String csum = Integer.toHexString(cint);

        
        if (csum.length()!=4){
            String f = csum.substring(0,1);
            csum=csum.substring(1);
            cint = Integer.parseInt(csum,16)+Integer.parseInt(f,16);
        }

        
        cint = 65535-cint;

        return Integer.toHexString(cint);

    }

    // Pads the payload to ensure it fits a certain size
    private static String addPad(String str){

        while (str.length()%8!=0){
            str=str+"0";
        }
        return str;
    }

    // Generates a random identification field for each packet
    private static String setIdField(){

        Random r = new Random();
        String id_field = Integer.toHexString(r.nextInt(65535+1));

        
        if (id_field.length()==1){
            System.out.println("Yes");
            return "000"+id_field;
        }
        else if(id_field.length()==2){
            System.out.println("No");
            return "00"+id_field;
        }
        else if(id_field.length()==3){
            System.out.println("~");
            return "0"+id_field;
        }

        return id_field;

    }

    // Encodes the payload and other necessary information into a packet
    private static String encodeFunc(InetAddress ipClient, InetAddress ipServer, String payL){
        

        String headerLength = "45"; //4 refers to IPv4 and 5 corresponds to the header length (fixed)
        String tOS = "00"; //type of service (fixed)
        String flags ="4000"; //corresponds to the fragment offset of IP header fields (fixed)
        String ttl ="4006"; //40 corresponds to the TTL field, 06 corresponds to TCP, protocol field (fixed)
        String idField = setIdField() ; //identification field (variable)

 
        String clientIP = convertIPToHex(ipClient); 
        String serverIP = convertIPToHex(ipServer); 
        String payload = convertStrToHex(payL); 

        String len = getLength(payL); 
        String csum = calcChecks(headerLength+tOS+len+idField+flags+ttl+clientIP+serverIP);

        System.out.println("Checksum value: "+ (csum.toUpperCase())+"\n" );

        String data = headerLength+tOS+len+idField+flags+ttl+csum+clientIP+serverIP+addPad(payload);


        return splitFunc(data);
    }

    public static void main(String[] args) throws IOException {

    
        String ipDest =InetAddress.getLocalHost().getHostAddress();
        String ipSrs = InetAddress.getLocalHost().getHostAddress();
        String machineName = InetAddress.getLocalHost().getHostName(); 
        String payL = "COLOMBIA 2 - MESSI 0"; 
        
 

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        
        if(args.length!=4) {
            System.out.println("***********Welcome**************");
            System.out.println("Would you like to enter custom data? Type y for yes ");
            String option = input.readLine();
            if (option.toLowerCase().equals("y")){
                System.out.println("------------------------------------");
                System.out.println("\nEnter a Server/Destination IP:   ");
                ipDest = input.readLine();
                System.out.println("\nEnter Payload:   ");
                payL = input.readLine();
                System.out.println("------------------------------------");

            }  

        } else if (args.length == 4){
            ipDest =args[1];
            payL = args[3];
        }

        System.out.println("-----Thank You!-----\n");

        System.out.println("\n----- Provided data ------");
        System.out.println("Source IP: "+ ipSrs);
        System.out.println("Destination IP: "+ ipDest);
        System.out.println("Payload: "+ payL);
        System.out.println("------------------------\n");

        String data = encodeFunc(InetAddress.getByName(ipSrs), InetAddress.getByName(ipDest), payL);

        new PacketSender(machineName, 4999, data);


        myClient.close();
        output.close();
        input.close();

    }
    
}
