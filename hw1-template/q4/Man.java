import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Man {
    String hostAddress = "localhost";
    private int udpPort;
    private String name;
    private List<Woman> preferences;
    Integer engagedIndex;

    public Man(int udpPort, String name) {
        this.udpPort = udpPort;
        this.name = name;
    }

    public void start() throws Exception {
        DatagramSocket clientUDPSocket = new DatagramSocket(udpPort);
        Thread udpThread = new Man.DatagramServer(clientUDPSocket);
        udpThread.start();
    }

    public void handleRequest(String request) throws IOException {
        Scanner scanner = new Scanner(request);
        if (scanner.next().equals("proposal")) {
            processProposal(scanner.nextInt());
        } else {
            System.out.println(name + ": Sorry. \"" + request + "\" wasn't a valid request.");
        }
    }

    public void processProposal(int requestPort) throws IOException {
        Woman proposer = preferences.stream()
                .filter(woman -> woman.getUdpPort() == requestPort)
                .findAny()
                .orElse(null);
        if (proposer != null) {
//            System.out.printf("%s: received proposal from %s%n proposer index: %d%n engaged index:%d%n", name, proposer.getName(), preferences.indexOf(proposer), engagedIndex);
            if (engagedIndex == null) {
                engagedIndex = preferences.indexOf(proposer);
                logEngagement();
//                System.out.printf("%s: accepted %s%n", name, proposer.getName());
            } else if (engagedIndex < preferences.indexOf(proposer)) {
                sendReject(proposer.getUdpPort());
//                System.out.printf("%s: rejected %s%n", name, proposer.getName());
            } else {
                sendReject(preferences.get(engagedIndex).getUdpPort());
                engagedIndex = preferences.indexOf(proposer);
                logEngagement();
//                System.out.printf("%s: accepted %s%n", name, proposer.getName());
            }
        }
    }

    public void logEngagement() throws IOException {
        sendUDPRequestNoResponse(String.format("engaged %d", udpPort), 50000);
    }

    public void sendReject(int udpPort) throws IOException {
        sendUDPRequestNoResponse(String.format("reject %d", this.udpPort), udpPort);
    }

    public class DatagramServer extends Thread {
        DatagramSocket clientSocket;
        DatagramPacket datapacket, returnpacket;

        public DatagramServer(DatagramSocket clientSocket) {
            this.clientSocket = clientSocket;
//            System.out.println(String.format("%s: UDP Server running", name));
        }

        public void run() {
            try {
                byte[] buf = new byte[1024];
                while (true) {
                    datapacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(datapacket);
                    String clientRequest = new String(datapacket.getData(), 0, datapacket.getLength());

                    handleRequest(clientRequest);
                }
            } catch (IOException e) {
                System.err.println(e);
            } finally {
                clientSocket.close();
            }
        }
    }

    public void sendUDPRequestNoResponse(String requestParameters, int udpPort) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(hostAddress);
        byte[] buf = requestParameters.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
        socket.send(packet);
        socket.close();
    }

    public List<Woman> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Woman> preferences) {
        this.preferences = preferences;
    }

    public String getName() {
        return name;
    }

    public int getUdpPort() {
        return udpPort;
    }

    @Override
    public String toString() {
        return String.format("port: %d, name: %s", udpPort, name);
    }
}