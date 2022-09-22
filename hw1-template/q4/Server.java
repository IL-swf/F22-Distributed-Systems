import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

  static int tcpPort;
  static int udpPort;
  static int currentOrderId = 0;
  static final String ORDERS_FILE_PATH = "C:\\\\Users\\\\betty\\\\IdeaProjects\\\\F22-Distributed-Systems\\\\hw1-template\\\\q4\\\\input\\\\orders.txt";

  static HashMap<String, Integer> inventory = new HashMap<>();
  static HashMap<Integer, Order> orders = new HashMap<>();

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

    // Load the inventory file
    File inventoryInput = new File(fileName);
    Scanner fileInput = new Scanner(inventoryInput);
    while (fileInput.hasNext()) {
      String nextItem = fileInput.next();
      int nextQuantity = fileInput.nextInt();

      inventory.put(nextItem, nextQuantity);
    }

    // Load the orders file
    File ordersInput = new File(ORDERS_FILE_PATH);
    fileInput = new Scanner(ordersInput);
    while (fileInput.hasNext()) {
      String userName = fileInput.next();
      int orderId = fileInput.nextInt();
      String productName = fileInput.next();
      String quantity = fileInput.next();
      String status = fileInput.next();

      Order order = new Order(orderId, userName, productName, Integer.parseInt(quantity), Order.OrderStatus.valueOf(status));
      orders.put(order.orderId, order);
      currentOrderId = Math.max(currentOrderId, orderId);
    }

    DatagramSocket clientUDPSocket = new DatagramSocket(udpPort);
    Thread udpThread = new DatagramServer(clientUDPSocket);
    udpThread.start();

    ServerSocket serverSocket = new ServerSocket(tcpPort);
    while (true) {
      Socket clientTCPSocket = serverSocket.accept();
      TCPServer tcpServer = new TCPServer(clientTCPSocket);
      tcpServer.start();
    }
  }

  public static String handleRequest(String clientRequest) {
    Scanner clientScanner = new Scanner(clientRequest);
    while (clientScanner.hasNext()) {
      switch (clientScanner.next()) {

        case "purchase" -> {
          String userName = clientScanner.next();
          String product = clientScanner.next();
          int quantity = clientScanner.nextInt();

          System.out.println("SERVER: Purchase: " + userName + " " + product + " " + quantity);

          return processPurchase(userName, product, quantity);
        }

        case "cancel" -> {
          int orderId = clientScanner.nextInt();

          System.out.println("SERVER: Cancel: " + orderId);

          return processCancel(orderId);
        }

        case "search" -> {
          String userName = clientScanner.next();

          System.out.println("SERVER: Search: " + userName);

          List<String> userOrders = processSearch(userName);

          if (userOrders.isEmpty()) return "No order found for " + userName;

          return userOrders.toString();
        }

        case "list" -> {
          System.out.println("SERVER: List");

          final String[] inventoryResponse = {""};

          inventory.forEach((productName, quantity) -> inventoryResponse[0] = inventoryResponse[0].concat(productName + " " + quantity + " "));

          return inventoryResponse[0];
        }
      }
    }
    return "Sorry. \"" + clientRequest + "\" wasn't a valid request.";
  }

  public static String processPurchase(String userName, String productName, int quantity) {
    if (!inventory.containsKey(productName)) return "Not Available - We do not sell this product";
    if (inventory.get(productName) < quantity) return "Not Available - Not enough items";

    Order thisOrder = orderFactory(userName, productName, quantity);

    orders.put(thisOrder.orderId, thisOrder);
    inventory.compute(productName, (key, value) -> value - quantity);

    return String.format("Your order has been placed, %d %s %s %d", thisOrder.orderId, userName, productName, quantity);
  }

  public static String processCancel(int orderId) {
    if (!orders.containsKey(orderId)) return orderId + " not found, no such order";

    Order order = orders.get(orderId);
    order.orderStatus = Order.OrderStatus.CANCELLED;
    orders.put(orderId, order);

    inventory.compute(order.productName, (key, value) -> value + order.quantity);

    return "Order " + orderId + " is cancelled";
  }

  public static List<String> processSearch(String userName) {
    List<String> userOrders = new ArrayList<>();
    orders.forEach((orderId, order) -> {
      if (order.userName == userName) userOrders.add(order.toString());
    });
    return userOrders;
  }

  public static Order orderFactory(String userName, String productName, int quantity) {
    currentOrderId++;
    return new Order(currentOrderId, userName, productName, quantity);
  }

  public static class Order {
    int orderId;
    String userName;
    String productName;
    int quantity;
    OrderStatus orderStatus;

    public enum OrderStatus {ACTIVE, COMPLETE, CANCELLED}

    public Order(int orderId, String userName, String productName, int quantity) {
      this.orderId = orderId;
      this.userName = userName;
      this.productName = productName;
      this.quantity = quantity;
      this.orderStatus = OrderStatus.ACTIVE;
    }

    public Order(int orderId, String userName, String productName, int quantity, OrderStatus orderStatus) {
      this.orderId = orderId;
      this.userName = userName;
      this.productName = productName;
      this.quantity = quantity;
      this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
      return orderId + ", " + productName + ", "+ quantity;
    }
  }

  public static class DatagramServer extends Thread {
    DatagramSocket clientSocket;
    DatagramPacket datapacket, returnpacket;

    public DatagramServer(DatagramSocket clientSocket) {
      this.clientSocket = clientSocket;
      System.out.println("UDP Server running");
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
    Socket clientSocket;

    public TCPServer(Socket clientSocket){
      this.clientSocket = clientSocket;
      System.out.println("New TCP Thread");
    }

    public void run() {

      try {
        System.out.println("Connected to client: " + clientSocket.toString());

        BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);

        String clientRequest = fromClient.readLine();

        String serverResponse = handleRequest(clientRequest);
        toClient.println(serverResponse);

        clientSocket.close();
        toClient.close();
        fromClient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
