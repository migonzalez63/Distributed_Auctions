package Bank;

import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class AgentProxy creates a proxy that keeps its socket connection
 * open in order to send auction updates and handle auction house deregistrations
 * from bank
 */
public class AgentProxy implements Runnable {
    private int id;
    private ObjectOutputStream out;
    private BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();
    private boolean isActive;

    /**
     * Constructor that creates Agent Proxy with a socket and ID
     * @param out
     * @param id
     */
    public AgentProxy(ObjectOutputStream out, int id) {
        this.out = out;
        this.id = id;
        isActive = true;
    }

    /**
     * While the thread is alive, the proxy will take in messages from BlockingQueue
     * and process them. It will only received messages when a new
     * Auction House is created or it has deregistered from bank
     */
    @Override
    public void run() {
        while(isActive) {
            try {
                Message incomingMessage = inbox.take();
                processMessage(incomingMessage);
            } catch (InterruptedException e) {
                isActive = false;
            }
        }
    }

    /**
     * Places a message on the proxies inbox to be processed
     * @param message
     */
    public void putMessage(Message message) {
        try {
            inbox.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the ID of the proxy
     * @return Integer indicating id
     */
    public int getID() {
        return id;
    }

    /**
     * Processes a message and sets the appropiate reply.
     * @param message
     */
    private void processMessage(Message message) {
        Message reply;

        /*
         * We send the same message with the new updated auction house list
         * to the agent
         */
        switch (message.getMessageType()) {
            case NEW_AUC:
            case DEREGISTRATION:
                reply = new Message(MessageType.UPDATE_LIST);
                reply.setAuctionList(Bank.getBusinessMembers());
                try {
                    out.writeObject(reply);
                } catch (IOException e) {
                    Bank.removeMember(id);
                    Bank.removeAgent(id);
                }
                break;
        }
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
