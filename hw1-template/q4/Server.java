import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
  public static void main(String[] args) throws FileNotFoundException {
    String[] testInputs = {"8080", "8090", "C:\\Users\\betty\\IdeaProjects\\F22-Distributed-Systems\\hw1-template\\q4\\input\\inventory.txt"};
    args = testInputs;

    int tcpPort;
    int udpPort;

    HashMap inventory = new HashMap<String, Integer>();

    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    tcpPort = Integer.parseInt(args[0]);
    udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];

    // parse the inventory file
    File inventoryInput = new File(fileName);
    Scanner fileInput = new Scanner(inventoryInput);
    while (fileInput.hasNext()) {
      String nextItem = fileInput.next();
      int nextQuantity = fileInput.nextInt();

      inventory.put(nextItem, nextQuantity);
    }

    Thread udpThread = new DatagramServer(1024, udpPort);
    udpThread.start();

    //System.out.println(inventory);

    // TODO: handle request from clients
  }

  public static DatagramPacket handleRequest(DatagramPacket clientRequest) {
    Scanner clientScanner = new Scanner(new String(clientRequest.getData(), 0, clientRequest.getLength()));
    while (clientScanner.hasNext()) {
      switch (clientScanner.next()) {
        case "purchase" -> System.out.println("SERVER: Purchase Request");
        case "cancel" -> System.out.println("SERVER: Cancel Request");
        case "search" -> System.out.println("SERVER: Search Request");
        case "list" -> System.out.println("SERVER: List Request");
      }
    }

    DatagramPacket returnDatagramPacket = new DatagramPacket(
            clientRequest.getData(),
            clientRequest.getLength(),
            clientRequest.getAddress(),
            clientRequest.getPort());
    return returnDatagramPacket;
  }

  public static class DatagramServer extends Thread {
    int len;
    int udpPort;
    DatagramPacket datapacket, returnpacket;

    public DatagramServer(int packetLength, int port) {
      len = packetLength;
      udpPort = port;
    }

    public void run() {
      try {
        DatagramSocket datasocket = new DatagramSocket(udpPort);
        byte[] buf = new byte[len];
        while (true) {
          datapacket = new DatagramPacket(buf, buf.length);
          datasocket.receive(datapacket);
          returnpacket = handleRequest(datapacket);
          datasocket.send(returnpacket);
        }
      } catch (SocketException e) {
        System.err.println(e);
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }

  public class TCPServer extends Thread {

  }
}
