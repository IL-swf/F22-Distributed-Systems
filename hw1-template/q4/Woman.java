import java.io.*;
import java.net.*;
import java.util.*;

public class Woman {
    String hostAddress = "localhost";
    private final int udpPort;
    private final String name;
    private List<Man> preferences;
    private List<Man> ranks;
    private HashMap<Integer, Integer> constraints = new HashMap<>();

    private int proposalCounter = 0;

    DatagramSocket socket;

    public Woman(int udpPort, String name) {
        this.udpPort = udpPort;
        this.name = name;
    }

    public void start() throws Exception {
        socket = new DatagramSocket(udpPort);
        Thread udpThread = new Woman.DatagramServer(socket);
        udpThread.start();
    }

    public void handleRequest(String clientRequest) throws IOException {
        logActive();
        Scanner clientScanner = new Scanner(clientRequest);
        while (clientScanner.hasNext()) {
            switch (clientScanner.next()) {

                case "initiate" -> initialize();

                case "reject" -> processReject(clientScanner.nextInt());

                case "advance" -> processAdvance(clientScanner.nextInt());

                default -> System.out.println("Sorry. \"" + clientRequest + "\" wasn't a valid request.");
            }
        }
        logPaused();
    }

    public void logActive() throws IOException {
        sendUDPRequestNoResponse(String.format("active %d", udpPort), 50000);
    }
    public void logPaused() throws IOException {
        sendUDPRequestNoResponse(String.format("paused %d", udpPort), 50000);
    }

    public void initialize() throws IOException {
        sendAdvanceRequests();
        sendProposal();
    }

    public void processReject(int rejectPort) throws IOException {
//        System.out.printf("%s: rejected by %s%n", name, preferences.stream().filter(man -> man.getUdpPort() == rejectPort).findAny().get().getName());
        if (preferences.get(proposalCounter).getUdpPort() == rejectPort) {
            if (proposalCounter == preferences.size()) {
//                System.out.printf("%s: failed%n", name);
                sendUDPRequestNoResponse("failure", 50000);
            } else {
                proposalCounter++;
                sendAdvanceRequests();
                sendProposal();
            }
        }
    }

    public void processAdvance(int manPort) throws IOException {
        Man proposer = preferences.stream()
                .filter(man -> man.getUdpPort() == manPort)
                .findAny()
                .get();
        while(preferences.indexOf(proposer) > proposalCounter) {
            proposalCounter++;
            sendAdvanceRequests();
        }
        sendProposal();
    }

    public void sendAdvanceRequests() {
        if (!constraints.isEmpty()){
            constraints.forEach((woman, man) -> {
                try {
                    if (preferences.get(proposalCounter).getUdpPort() == man && woman != udpPort) {
                        sendUDPRequestNoResponse(String.format("advance %d", man), woman);
                        System.out.printf("%s: advance %d %d%n", name, man, woman);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void sendProposal() throws IOException {
        sendUDPRequestNoResponse(String.format("proposal %d", udpPort), preferences.get(proposalCounter).getUdpPort());
//        System.out.printf("%s: sent proposal to %s%n", name, preferences.get(proposalCounter).getName());
    }

    public class DatagramServer extends Thread {
        DatagramSocket clientSocket;
        DatagramPacket dataPacket;

        public DatagramServer(DatagramSocket clientSocket) {
            this.clientSocket = clientSocket;
//            System.out.println(String.format("%s: UDP Server running", name));
        }

        public void run() {
            try {
                byte[] buf = new byte[1024];
                while (true) {
                    dataPacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(dataPacket);
                    String clientRequest = new String(dataPacket.getData(), 0, dataPacket.getLength());

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
        InetAddress address = InetAddress.getByName(hostAddress);
        byte[] buf = requestParameters.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
        socket.send(packet);
    }

    public List<Man> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Man> preferences) {
        this.preferences = preferences;
    }

    public HashMap<Integer, Integer> getConstraints() {
        return constraints;
    }

    public void setConstraints(HashMap<Integer, Integer> constraints) {
        this.constraints = constraints;
    }

    public List<Man> getRanks() {
        return ranks;
    }

    public void setRanks(List<Man> ranks) {
        this.ranks = ranks;
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