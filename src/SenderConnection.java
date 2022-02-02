import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class SenderConnection {
    public String aliveMessage;
    public int packetCount;

    public void sendMessage(String ip, int senderPort, int receiverPort, String fileName, int timeout, boolean reliable) throws IOException, InterruptedException {
        final int maxDataSize = 216;

        // Initialize memory data
        String fileContent = new String(Files.readAllBytes(Paths.get(fileName)));
        byte[] buffer = new byte[0];

        // Initialize Datagram data
        DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
        DatagramSocket ds = new DatagramSocket(null);
        ds.bind(new InetSocketAddress(ip, senderPort));

        // Default timeout is 2 milliseconds or more,
        // case where microseconds are less than that is handelled below
        int timeoutToSend = timeout / 1000 > 2 ? timeout / 1000 : 2;
        ds.setSoTimeout(timeoutToSend);

        int timeoutWait = 0; // Counter to make sure we don't get stuck in an infinite loop on the 10th packet
        int EOT = 0;
        this.packetCount = 0; // Initialize packet count

        int len = fileContent.length() / maxDataSize;
        // Loop over all packets in the file
        for (int i = 0; i < len + 2; i++) {

            if (i < len + 1) {
                buffer = fittedBufferData(fileContent, maxDataSize, i);
            } else if (i == len + 1 && EOT == 0) {
                // We've reached the end of the file send EOT packet
                buffer = new byte[]{(byte) '\b', (byte) 4};
                EOT++;
            }

            // We're in unreliable and reached the 10th packet that hasn't already been sent
            if (!reliable && i % 10 == 0 && timeoutWait == 0) {
                // If we have a timeout shorter than 2ms we wait by that time
                if (timeoutToSend < 2)
                    TimeUnit.MICROSECONDS.sleep(timeout);

                // Increment timeoutWait so we don't get stuck in an infinite loop
                timeoutWait++;
            } else {
                // Normal case or packet % 10 != 0
                timeoutWait = 0; // Reset timeoutWait
                this.packetCount++; // Increment packetCount
                Sender.sent.setText(Sender.packetMessage + this.packetCount); // Update UI
                ds.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), receiverPort)); // Send packet
            }

            // Get ACK from Receiver
            try {
                ds.receive(dp);
                int ackReceived = -1;

                for (byte b : dp.getData()) {
                    String letter = String.valueOf((char) b);
                    // Make sure that the correct sequence number and proper ACK
                    if (letter.equals("0") || letter.equals("1") || letter.equals("4"))
                        ackReceived = Integer.parseInt(letter);
                }

                // If we have an invalid ACK (the Receiver didn't get the packet properly)
                if (ackReceived != i % 2 && ackReceived != 4) {
                    i--; // Go back and send it again
                }
            } catch (SocketTimeoutException exception) {
                // We've timed out so go back and send it again
                i--;
            }
        }
        ds.close();
    }

    public void checkAlive (String ip, int senderPort, int receiverPort) throws IOException {
        final int timeout = 500; // Standard set value of 500ms for checkAlive

        // Initialize Datagram data
        DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
        DatagramSocket ds = new DatagramSocket(null);
        ds.bind(new InetSocketAddress(ip, senderPort));
        ds.setSoTimeout(timeout);
        String val = "IS_ALIVE__";
        //byte[] buffer = new byte[]{(byte) '\t', (byte) 4}; // Send an empty packet with just an EOT
        byte[] buffer = fittedBufferData(val, 216, 0);
        ds.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), receiverPort));
        int ackReceived = -1;

        try {
            ds.receive(dp);
            for (byte b : dp.getData()) {
                String letter = String.valueOf((char) b);
                if (letter.equals("0") || letter.equals("1") || letter.equals("4"))
                    ackReceived = Integer.parseInt(letter);
            }
        } catch (SocketTimeoutException exception) {
            this.aliveMessage = "The server is DEAD!";
        }
        ds.close();

        if (ackReceived != -1)
            this.aliveMessage = "The server is ALIVE!";
    }


    // Create a fitted buffer for the packet (i.e. a buffer that holds all of the file data)
    private byte[] fittedBufferData(String data, int maxSize, int packetIndex) {
        byte[] buffer = new byte[maxSize + 1]; // Initialize size to be 1 more for sequence number

        int prevIndex = maxSize * packetIndex; // The previousIndex of the last datagram (increments of MDS)
        int endIndex = maxSize * (packetIndex + 1); // Current max index size

        if (packetIndex == data.length() / maxSize)
            endIndex = data.length();

        int len = endIndex - prevIndex; // Number of packets in the datagram

        for (int i = 0; i < len; i++) {
            buffer[i] = (byte) data.charAt(prevIndex + i);
        }

        // Will always be 0 or 1 (even or odd packet)
        // Add the sequence number to the end of each datagram
        buffer[maxSize] = (byte) (packetIndex % 2);
        return buffer;
    }

}
