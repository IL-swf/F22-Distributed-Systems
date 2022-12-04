import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Environment {
    String hostAddress = "localhost";
    DatagramSocket socket;
    int udpPort;
    HashMap<Integer, String> women;
    HashMap<Integer, String> men;

    Set<Integer> engagedMen = new HashSet<>();
    Set<Integer> activeWomen = new HashSet<>();

    public Environment(int udpPort, HashMap<Integer, String> women, HashMap<Integer, String> men) {
        this.udpPort = udpPort;
        this.women = women;
        this.men = men;
    }

    public void start() throws Exception {
        socket = new DatagramSocket(this.udpPort);
        Thread udpThread = new Environment.DatagramServer(socket);
        udpThread.start();

        seedHumans();
        initiateMatching();
    }

    public void initiateMatching() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        women.forEach((udpPort, woman) -> {
            try {
                sendUDPRequestNoResponse(String.format("initiate"), udpPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void seedHumans() throws Exception {
        List<Man> seedMen = new ArrayList<>();
        men.forEach((udpPort, name) -> {
            seedMen.add(new Man(udpPort, name));
        });
        List<Woman> seedWomen = new ArrayList<>();
        women.forEach((udpPort, name) -> {
            seedWomen.add(new Woman(udpPort, name));
        });

        for (Woman seedWoman : seedWomen) {
            // Seed men with non-conflicting preferences
//            seedMen.add(seedMen.remove(0));
            seedWoman.setPreferences(new ArrayList<>(seedMen));

            HashMap<Integer, Integer> testConstraints = new HashMap<>();
            testConstraints.put(30002, 40000);
            testConstraints.put(30000, 40003);
            seedWoman.setConstraints(new HashMap<>(testConstraints));

            seedWoman.start();

//            System.out.println(seedWomen.get(i));
//            System.out.println(seedMen);
        }

        for (Man seedMAN : seedMen) {
            // Seed men with non-conflicting preferences
            seedWomen.add(seedWomen.remove(0));
            seedMAN.setPreferences(new ArrayList<>(seedWomen));
            seedMAN.start();

//            System.out.println(seedMen.get(i));
//            System.out.println(seedWomen);
        }
    }

    public void handleRequest(String clientRequest) throws IOException {
        Scanner clientScanner = new Scanner(clientRequest);
        while (clientScanner.hasNext()) {
            switch (clientScanner.next()) {
                case "failed" -> System.out.println("NO CSM");

                case "engaged" -> handleEngagement(clientScanner.nextInt());

                case "active" -> handleActive(clientScanner.nextInt());

                case "paused" -> handlePassive(clientScanner.nextInt());
            }
        }
    }

    public void handleEngagement(int engagedPort) {
        engagedMen.add(engagedPort);
        System.out.printf("ENGAGED: %d%n", engagedPort);
        checkCompleteness();
    }

    public void handleActive(int port) {
        activeWomen.add(port);
        System.out.printf("ACTIVE: %d%n", port);
        checkCompleteness();
    }

    public void handlePassive(int port) {
        activeWomen.remove(port);
        System.out.printf("PASSIVE: %d%n", port);
        checkCompleteness();
    }

    public void checkCompleteness() {
        if (engagedMen.size() == women.size() && activeWomen.isEmpty() ) System.out.println("COMPLETE");
    }

    public class DatagramServer extends Thread {
        DatagramSocket socket;
        DatagramPacket datapacket, returnpacket;

        public DatagramServer(DatagramSocket socket) {
            this.socket = socket;
//            System.out.println(String.format("%s: UDP Server running", name));
        }

        public void run() {
            try {
                byte[] buf = new byte[1024];
                while (true) {
                    datapacket = new DatagramPacket(buf, buf.length);
                    socket.receive(datapacket);
                    String clientRequest = new String(datapacket.getData(), 0, datapacket.getLength());

                    handleRequest(clientRequest);
                }
            } catch (IOException e) {
                System.err.println(e);
            } finally {
                socket.close();
            }
        }
    }

    public void sendUDPRequestNoResponse(String requestParameters, int udpPort) throws IOException {
        InetAddress address = InetAddress.getByName(hostAddress);
        byte[] buf = requestParameters.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
        socket.send(packet);
    }
}