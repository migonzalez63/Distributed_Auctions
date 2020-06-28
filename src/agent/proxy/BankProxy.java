package agent.proxy;

import agent.AgentManager;
import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Designed to handle all communication between an agent and the bank.
 */
public class BankProxy implements Runnable {

    //info
    private AgentManager manager;
    private boolean active;

    //streams
    Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Creates a BankProxy and connects it to the Bank at the given host and
     * port.
     * @param manager AgentManager to reference
     * @param hostName host of bank
     * @param port port of bank
     * @param amount initial account balance
     * @throws IOException
     */
    public BankProxy(AgentManager manager, String hostName, int port,
                     int amount) throws IOException {
        this.manager = manager;

        // initialize sockets and streams
        socket = new Socket(hostName, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        active = true;

        // send registration message
        Message register = new Message(MessageType.NEW_AGENT);
        register.setAmount(amount);
        sendMessage(register);
    }

    /**
     * Tells the bank to transfer money from the agent to the given other
     * account.
     * @param auctionID account to transfer to
     * @param amount amount to transfer
     */
    public void transfer(int auctionID, int amount) {
        Message transfer = new Message(MessageType.TRANSFER);
        transfer.setAccountID1(manager.id());
        transfer.setAccountID2(auctionID);
        transfer.setAmount(amount);
        sendMessage(transfer);
    }

    /**
     * Sends a given message to the connected Bank.
     * @param message message to send
     */
    public void sendMessage(Message message) {
        try {
            AgentManager.debug("To Bank: " + message.getMessageType());
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to Bank");
        }
    }

    /**
     * Reads in a message from the input stream.
     */
    private void readMessage() {
        try {
            Message newMessage = (Message) in.readObject();
            AgentManager.debug("Bank: " + newMessage.getMessageType());
            execute(newMessage);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading message from Bank");
            active = false;
            try {
                socket.close();
            } catch (IOException f) {
                f.printStackTrace();
            }
        } catch (NullPointerException e) {
            System.out.println("Closed");
        }
    }

    /**
     * Executes tasks based on the input message.
     * @param input input message
     */
    private void execute(Message input) {
        switch(input.getMessageType()) {
            case UPDATE_BALANCE:
                manager.setBalance(input.getAmount());
                break;
            case REGISTRATION_CONFIRM:
                manager.setBalance(input.getAmount());
                manager.setID(input.getAccountID1());
            case UPDATE_LIST:
                manager.updateAuctionList(input.getAuctionList());
                break;
        }
    }

    /**
     * De-registers with the bank and closes the sockets.
     */
    public void close() {
        Message deregister = new Message(MessageType.DEREGISTRATION);
        deregister.setAccountID1(manager.id());
        sendMessage(deregister);
        active = false;
    }

    /**
     * Main run method.
     */
    public void run() {
        while (active) {
            readMessage();
        }
    }
}
