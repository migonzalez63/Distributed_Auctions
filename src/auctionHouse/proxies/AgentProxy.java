/**
 * Class: AgentProxy
 * Description: Creates a socket and manages interactions from agent.
 * @author Tanner Randall Hunt
 */
package auctionHouse.proxies;

import auctionHouse.AuctionHouse;
import auctionHouse.auctionBlock.items.Item;
import auctionHouse.auctionBlock.items.ItemInfo;
import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AgentProxy extends Observable implements Runnable {
    private boolean isActive = true;

    private BlockingQueue<Message> outBox = new LinkedBlockingQueue<>();
    private int auctionID;
    private int agentID;
    private AuctionHouse handler;
    private Socket socket;
    private String ipAddress;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Creates a socket and manages interactions from agent.
     * @param socket new socket
     * @param handler auction house
     * @param auctionID auction id
     * @throws IOException
     */
    public AgentProxy(Socket socket, AuctionHouse handler, int auctionID) throws IOException {
        this.socket = socket;
        this.handler = handler;
        this.auctionID = auctionID;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.ipAddress = socket.getInetAddress().toString();
        initWriting();
    }

    private void initWriting(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(isActive)
                try {
                    writeMessage(outBox.take());
                }catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        };
        new Thread(runnable).start();
    }
    /**
     * Send Auction End Message.
     */
    public  void notifyAuctionEnd(){
        sendMessage(null,MessageType.AUCTION_END);
    }
    /**
     * Send Item Update Message.
     * @param item item that was updated.
     */
    public void notifyItemUpdate(Item item){
        sendMessage(item,MessageType.UPDATE);
    }
    /**
     * Send New_Item Message.
     * @param item new Item.
     */
    public  void notifyNewItem(Item item){
        sendMessage(item,MessageType.NEW_ITEM);
    }
    /**
     * Send Item Expiration Message.
     * @param item item where time is elapsing.
     */
    public  void notifyItemExpiration(Item item){
       sendMessage(item,MessageType.EXPIRED);
    }
    /**
     * Send Outbid message to agent
     * @param item item agent was outbid on.
     */
    public  void notifyItemOutBid(Item item){
       sendMessage(item,MessageType.OUTBID);

    }
    /**
     * Send Win message to agent
     * @param item item won.
     */
    public void notifyItemWin(Item item){
         sendMessage(item,MessageType.WIN);

    }
    /**
     * Send Elapse message to agent
     * @param item item where time is elapsing.
     */
    public void notifyItemElapse(Item item){
       sendMessage(item,MessageType.ELAPSED);
    }

    //sends a message to the agents
    private void sendMessage(Item item, MessageType messageType){
        ItemInfo itemInfo = getItemInfo(item);
        Message message = new Message(messageType);
        message.setAccountID1(auctionID);
        message.setItemInfo(itemInfo);
        putInOutbox(message);
    }

    //Extracts item info and puts it into ItemInfo object
    private ItemInfo getItemInfo(Item item) {
        if(item == null) return null;
        return new ItemInfo(item.getItemID(),
                item.getCurrentBid(),
                item.getTimeLeft());
    }


    /**
     * Gets the agent id.
     * @return agent id.
     */
    public int getAgentID() {
        return agentID;
    }

    //Write Messages
    private void writeMessage(Message message)   {
        if(!socket.isClosed()) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Agent Socket Closed on Write");
            }
        }
    }


    private void putInOutbox(Message message){
        try {
            outBox.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    //Handle Incoming messages
    private void processMessage(Message message)  {
        Message outGoing;
        switch(message.getMessageType()){
            case NEW_AGENT:{

                this.agentID = message.getAccountID1();
                System.out.println("\nNew Agent Registered: "+agentID+"\n");
                outGoing = new Message(MessageType.ITEM_LIST);
                outGoing.setAccountID1(auctionID);
                outGoing.setItemList(handler.getItemsOnSale());
                handler.registerAgentProxy(this);
                putInOutbox(outGoing);
                break;
            }
            case NEW_BID:{
                String itemID = message.getItemID();
                int agentID = message.getAccountID1();
                int bidProposal = message.getAmount();

                System.out.println("Processing bid from " + agentID);
                System.out.println("\nNew Bid From: "+ agentID);
                System.out.println("Item: "+itemID);
                System.out.println("Amount: "+bidProposal);

                Item item = handler.proposeBid(itemID,agentID,bidProposal);
                if(item != null){
                    System.out.println("Bank Accepted Bid From: "+agentID+" on "
                            + itemID );
                    outGoing = new Message(MessageType.ACCEPTED);
                }else {
                    outGoing = new Message(MessageType.REJECTED);
                    System.out.println("Bank Rejected Bid From: "+agentID+" on "
                            + itemID );
                }
                outGoing.setAccountID1(auctionID);
                outGoing.setItemID(itemID);
                outGoing.setAmount(bidProposal);
                outGoing.setItemInfo(getItemInfo(item));
                putInOutbox(outGoing);
                System.out.println("$Message Sent$");
                break;
            }
            case DEREGISTRATION:{
                shutDown();
                break;
            }

        }

    }

    @Override
    public void run() {
        while(isActive){
            try {
                Message newMessage = (Message) in.readObject();
                processMessage(newMessage);
            } catch (  IOException | ClassNotFoundException e) {
                System.err.println("Agent Socket Closed on Read");
                shutDown();
                break;
            }
        }
        System.out.println("Agent Shutdown");
    }

    //Shut down the agent remove from auction house
    private void shutDown(){
        try {
            isActive = false;
            handler.removeAgentProxy(agentID);
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
