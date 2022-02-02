import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Receiver {
    static DatagramSocket ds;

    public static void main(String[] args) throws IOException {
        // Get command line arguments
        String address = args[0];
        int receiverPort = Integer.parseInt(args[1]);
        int senderPort = Integer.parseInt(args[2]);
        String outputFileName = args[3];

        // Create new file if not existing
        if (new File(outputFileName).createNewFile()) {
            System.out.println("File not found, created.");
        }

        // Initialize Datagram data
        ds = new DatagramSocket(null);
        ds.bind(new InetSocketAddress(address, receiverPort));
        DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);

        // Final data to write to file after reading all Datagrams
        StringBuilder finalData = new StringBuilder();
        ArrayList<String> datagrams = new ArrayList<String>();
        // Get total running time
        long startTime = 0;
        int messageCount = 0; // This is to find the start time of the first message

        System.out.println("Awaiting data...");

        // Loop infinitely until broken
        for (;;) {
            try {
                ds.receive(dp);
                if (messageCount == 0)
                    startTime = System.currentTimeMillis();

                String data = "";
                messageCount++;
                int seqNum = dp.getData()[dp.getLength() - 1];

                for (int i = 0; i < dp.getLength(); i++) {
                    data += (char) dp.getData()[i];
                }

                finalData.append(data);

                if (data.contains("IS_ALIVE__")) {
                    FileWriter myWriter = new FileWriter(outputFileName);
                    myWriter.write(finalData.toString());
                    myWriter.close();

                    long totalTransmission = System.currentTimeMillis() - startTime;
                    System.out.println("Total Running Time: " + totalTransmission + "ms, (" + totalTransmission / 1000 + "s)");

                    finalData = new StringBuilder();
                    messageCount = 0;
                    startTime = System.currentTimeMillis();
                }

                if (data.contains("\b") && seqNum == 4) {
                    long totalTransmission = System.currentTimeMillis() - startTime;

                    if (totalTransmission > 5) {
                        FileWriter myWriter = new FileWriter(outputFileName);
                        myWriter.write(finalData.toString());
                        myWriter.close();

                        System.out.println("Total Running Time: " + totalTransmission + "ms, (" + (double) (totalTransmission) / 1000 + "s)");
                    }

                    finalData = new StringBuilder();
                    messageCount = 0;
                }
//                else if (finalData.toString().trim().length() == 0 && data.contains("\b") && seqNum == 4) {
//                    finalData = new StringBuilder();
//                    messageCount = 0;
//                }

                String ack = "ACK " + seqNum;
                ds.send(new DatagramPacket(ack.getBytes(), ack.getBytes().length, InetAddress.getByName(address), senderPort));

            } catch (IOException exception) {
                break;
            }
        }
        ds.close();
    }
}

