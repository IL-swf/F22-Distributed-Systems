import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        String hostAddress;
        int tcpPort;
        int udpPort;

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

            if (tokens[0].equals("initiate")) {

            } else if (tokens[0].equals("reject")) {
            } else if (tokens[0].equals("advance")) {
            } else if (tokens[0].equals("list")) {
                System.out.println("You typed list!");
                String response = sendUDPRequest("list", hostAddress, udpPort);
                System.out.println(response);
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }

    public static String sendUDPRequest(String requestParameters, String hostAddress, int udpPort) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(hostAddress);
        byte[] buf = requestParameters.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
        socket.send(packet);
        buf = new byte[1024];
        DatagramPacket received = new DatagramPacket(buf, 1024);
        socket.receive(received);
        String serverResponse = new String(received.getData(), 0, received.getLength());
        socket.close();
        return serverResponse;
    }
}