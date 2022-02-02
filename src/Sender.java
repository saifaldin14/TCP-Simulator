import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.awt.event.*;

class Sender extends Component implements ActionListener {
    public static String packetMessage = "Number of Sent Packets: ";
    public static JLabel sent = new JLabel(packetMessage, SwingConstants.CENTER);

    private JLabel label = new JLabel("Welcome to your very own FTP Tool.", SwingConstants.CENTER);
    public JLabel isAliveLabel = new JLabel("Server still hasn't been checked yet", SwingConstants.CENTER);
    private JLabel ipAddy = new JLabel("IP Address: ");
    private JLabel port = new JLabel("Sender UDP Port Number: ");
    private JLabel blank = new JLabel("Reciever UDP Port Number: ");
    private JLabel fileName = new JLabel("Name of File: ");
    private JLabel timeout = new JLabel("Timeout: ");
    private JLabel runtime = new JLabel("THIS NEEDS TO BE LIVE DATA (Runtime)", SwingConstants.CENTER);

    private JFrame frame = new JFrame();
    private JButton send;
    private JButton isAlive;
    private JTextField ipAddyy;
    private JTextField portNumSend;
    private JTextField portNumRecieve;
    private JTextField file;
    private JTextField time;
    private JCheckBox checkboxReliable;

    private final SenderConnection senderConnection;

    public Sender() {
        senderConnection = new SenderConnection();

        // the clickable buttons
        isAlive = new JButton("ISALIVE?");
        send = new JButton("Send");

        //text fields
        ipAddyy = new JTextField(20);
        ipAddyy.setBounds(100, 20, 165, 25);

        portNumSend = new JTextField(20);
        portNumSend.setBounds(100, 20, 165, 25);

        portNumRecieve = new JTextField(20);
        portNumRecieve.setBounds(100,20,165,25);

        file = new JTextField(20);
        file.setBounds(100, 20, 165, 25);

        checkboxReliable = new JCheckBox("Reliable");
        checkboxReliable.setBounds(100, 20, 165, 25);
        checkboxReliable.setSelected(true);

        time = new JTextField(20);
        time.setBounds(100, 20, 165, 25);

        //Button click event listeners
        isAlive.addActionListener(this::aliveHandler);
        send.addActionListener(this::sendHandler);


        // the panel with the button and text
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(0, 1));
        panel.add(label);
        panel.add(isAliveLabel);
        panel.add(ipAddy);
        panel.add(ipAddyy);
        panel.add(port);
        panel.add(portNumSend);
        panel.add(blank);
        panel.add(portNumRecieve);
        panel.add(fileName);
        panel.add(file);
        panel.add(checkboxReliable);
        panel.add(isAlive);
        panel.add(send);
        panel.add(sent);
        panel.add(timeout);
        panel.add(time);

        // set up the frame and display it
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Stop and Wait 3.0 Simulation Application");
        frame.pack();
        frame.setVisible(true);
    }

    // process the button clicks
    public void actionPerformed(ActionEvent e) {

    }

    private void aliveHandler (ActionEvent e) {
        try {
            // Parse all JComponents into useful values
            String address = ipAddyy.getText();
            int senderPort = Integer.parseInt(portNumSend.getText());
            int receiverPort = Integer.parseInt(portNumRecieve.getText());

            // Background process to start sending so we don't block our GUI and its repainting
            new SwingWorker<Void, Void>() {
                @Override
                public Void doInBackground() throws IOException, InterruptedException {
                    senderConnection.checkAlive(address, senderPort, receiverPort);
                    isAliveLabel.setText(senderConnection.aliveMessage);
                    //senderConnection.updateAliveLabelGUI(isAliveLabel);
                    return null;
                }
            }.execute();
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Invalid port number(s), please enter a number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendHandler(ActionEvent e) {
        // Check if we are currently receiving or not based on button
        try {
            // Parse all JComponents into useful values
            String address = ipAddyy.getText();
            int senderPort = Integer.parseInt(portNumSend.getText());
            int receiverPort = Integer.parseInt(portNumRecieve.getText());
            String inputFileName = file.getText();
            int timeVal = Integer.parseInt(time.getText());
            boolean reliable = checkboxReliable.isSelected();

            // Background process to start sending so we don't block our GUI and its repainting
            new SwingWorker<Void, Void>() {
                @Override
                public Void doInBackground() throws IOException, InterruptedException {
                    senderConnection.sendMessage(address, senderPort, receiverPort, inputFileName, timeVal, reliable);
                    //sent.setText(packetMessage + senderConnection.packetCount);
                    return null;
                }
            }.execute();
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Invalid arguments, please enter valid arguments", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // create one Frame
    public static void main(String[] args) {
        new Sender();
    }

}