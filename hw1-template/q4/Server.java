import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

  static int tcpPort;
  static int udpPort;

  static HashMap<String, Integer> inventory = new HashMap<>();

  public static void main(String[] args) throws Exception {

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

    DatagramSocket clientSocket = new DatagramSocket(udpPort);
    Thread udpThread = new DatagramServer(clientSocket);
    udpThread.start();

    ServerSocket serverSocket = new ServerSocket(tcpPort);
    Thread tcpThread = new TCPServer(serverSocket);
    tcpThread.start();

    //System.out.println(inventory);

    // TODO: handle request from clients
  }

  public static String handleRequest(String clientRequest) {
    Scanner clientScanner = new Scanner(clientRequest);
    while (clientScanner.hasNext()) {
      switch (clientScanner.next()) {
        case "purchase" -> {
          System.out.println("SERVER: Purchase Request");
          String userName = clientScanner.next();
          String product = clientScanner.next();
          int quantity = clientScanner.nextInt();

          return clientRequest;
        }
        case "cancel" -> {
          System.out.println("SERVER: Cancel Request");
          return clientRequest;
        }
        case "search" -> {
          System.out.println("SERVER: Search Request");
          return clientRequest;
        }
        case "list" -> {
          System.out.println("SERVER: List Request");
          return inventory.toString();
        }
      }
    }
    return clientRequest;
  }

  public static class DatagramServer extends Thread {
    DatagramSocket clientSocket;
    DatagramPacket datapacket, returnpacket;
    int counter = 0;

    public DatagramServer(DatagramSocket clientSocket) {
      this.clientSocket = clientSocket;
      counter++;
      System.out.println("UDP Thread: " + counter);
    }

    public void run() {
      try {
        byte[] buf = new byte[1024];
        while (true) {
          datapacket = new DatagramPacket(buf, buf.length);
          clientSocket.receive(datapacket);
          String clientRequest = new String(datapacket.getData(), 0, datapacket.getLength());
          String serverResponse = handleRequest(clientRequest);
          returnpacket = new DatagramPacket(
                  serverResponse.getBytes(),
                  serverResponse.getBytes().length,
                  datapacket.getAddress(),
                  datapacket.getPort());
          clientSocket.send(returnpacket);
        }
      } catch (IOException e) {
        System.err.println(e);
      } finally {
        clientSocket.close();
      }
    }
  }

  public static class TCPServer extends Thread {
    ServerSocket serverSocket;

    public TCPServer(ServerSocket serverSocket){
      this.serverSocket = serverSocket;
      System.out.println("TCP Thread");
    }

    public void run() {

      try {
        System.out.println("Waiting for client");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Connected to client: " + clientSocket.toString());

        BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);

        String clientRequest = fromClient.readLine();

        String serverResponse = handleRequest(clientRequest);
        toClient.println(serverResponse);

        fromClient.close();
        toClient.close();
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
