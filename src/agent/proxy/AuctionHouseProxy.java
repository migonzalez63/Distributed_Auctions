package agent.proxy;

import agent.AgentManager;
import agent.AgentPair;
import auctionHouse.auctionBlock.items.ItemInfo;
import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Designed to handle all communication between the agent and an individual
 * auction house. Observed by an individual AuctionHouseTab that is the GUI
 * equivalent of this class. Stores items in an indexed list.
 */
public class AuctionHouseProxy extends Observable implements Runnable {

    // info
    private AgentManager manager;
    private InetSocketAddress address;
    private int auctionID;
    private boolean active;

    // streams
    Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // items and bidding
    private List<ItemInfo> itemList = new LinkedList<>();
    private Queue<Integer> expiredIndexes = new LinkedList<>();
    private int activeBids = 0;

    /**
     * Initializes a new AuctionHouseProxy that references a given manager
     * and connects to an AuctionHouse at a given address.
     * @param manager AgentManager to reference
     * @param address InetSocketAddress to connect to
     * @throws IOException
     */
    public AuctionHouseProxy(AgentManager manager, InetSocketAddress address) throws IOException {
        this.manager = manager;
        this.address = address;

        // initialize socket and streams
        socket = new Socket(address.getAddress(), address.getPort());
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        active = true;
        // send registration message
        Message register = new Message(MessageType.NEW_AGENT);
        register.setAccountID1(manager.id());
        sendMessage(register);
        System.out.println("Proxy initialized");
    }

    /**
     * Returns the ID of the connected AuctionHouse.
     * @return auction house id
     */
    public int id() {
        return auctionID;
    }

    /**
     * Returns the item at a specified index in the item list.
     * @param i index to get
     * @return item at index
     */
    public ItemInfo getItem(int i) {
        return itemList.get(i);
    }

    /**
     * Gets the index of an item within the list. Returns -1 if not in list.
     * @param item item to find index of
     * @return index of item
     */
    private synchronized int getIndex(ItemInfo item) {
        for (int i = 0; i < itemList.size(); i++) {
            if (item.equals(itemList.get(i))) return i;
        }
        return -1;
    }

    /**
     * Returns the number of items currently stored.
     * @return number of items
     */
    public int numItems() {
        return itemList.size();
    }

    /**
     * Checks if there are any active bids at this auction house.
     * @return true if any active bids
     */
    public boolean hasActiveBids() {
        return activeBids > 0;
    }

    /**
     * Places a bid for an item at this auction house.
     * @param itemID name of item to bid on
     * @param bid amount to bid
     */
    public void placeBid(String itemID, int bid) {
        AgentManager.debug("Item: " + itemID + "    Bid: " + bid);
        Message toSend = new Message(MessageType.NEW_BID);
        toSend.setAccountID1(manager.id());
        toSend.setAmount(bid);
        toSend.setItemID(itemID);
        sendMessage(toSend);
    }

    /**
     * Sends a given message to the connected AuctionHouse.
     * @param message message to send
     */
    private void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
            AgentManager.debug("To AH " + auctionID + ": " +
                    message.getMessageType());
        } catch (IOException e) {
            System.err.println("Error sending message to Auction House "
                    + auctionID);
        }
    }

    /**
     * Reads in a message from the input stream.
     */
    private void readMessage() {
        if (in == null) return;
        try {
            Message newMessage = (Message) in.readObject();
            AgentManager.debug("Auction House " + auctionID + ": " +
                            newMessage.getMessageType());
            execute(newMessage);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading message from Auction House "
                    + auctionID);
            close();
        }
    }

    /**
     * Executes tasks based on the input message.
     * @param input input message
     */
    private synchronized void execute(Message input) {
        // set up common queries
        ItemInfo item = input.getItemInfo();
        int itemIndex = -2;
        if (item != null && input.getMessageType() != MessageType.NEW_ITEM) {
            itemIndex = getIndex(item);
        }

        // handle message types
        switch(input.getMessageType()) {
            case ACCEPTED:
                activeBids++;
                manager.hold(input.getAmount());
                setChanged();
                notifyObservers(new AgentPair(MessageType.ACCEPTED, itemIndex));
                break;
            case REJECTED:
                setChanged();
                notifyObservers(new AgentPair(MessageType.REJECTED, itemIndex));
                break;
            case ITEM_LIST:
                auctionID = input.getAccountID1();
                itemList = input.getItemList();
                setChanged();
                notifyObservers(new AgentPair(MessageType.ITEM_LIST, 0));
                break;
            case OUTBID:
                activeBids--;
                int oldBid = itemList.get(itemIndex).getCurrentBid();
                manager.hold(-1 * oldBid);
            case ELAPSED:
            case UPDATE:
                itemList.set(itemIndex, item);
                setChanged();
                notifyObservers(new AgentPair(input.getMessageType(),
                        itemIndex));
                break;
            case EXPIRED:
                expiredIndexes.add(itemIndex);
                setChanged();
                notifyObservers(new AgentPair(input.getMessageType(),
                        itemIndex));
                break;
            case NEW_ITEM:
                int newIndex = expiredIndexes.remove();
                itemList.set(newIndex, input.getItemInfo());
                setChanged();
                notifyObservers(new AgentPair(input.getMessageType(),
                        newIndex));
                break;
            case WIN:
                activeBids--;
                expiredIndexes.add(itemIndex);
                manager.pay(item.getCurrentBid());
                manager.transfer(auctionID, item.getCurrentBid());
                setChanged();
                notifyObservers(item);
                break;
            case AUCTION_END:
                close();
                break;
        }
    }

    /**
     * Stops reading in messages and closes the socket.
     */
    public void close() {
        active = false;
        try {
            socket.close();
        } catch (IOException f) {
            f.printStackTrace();
        }
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
