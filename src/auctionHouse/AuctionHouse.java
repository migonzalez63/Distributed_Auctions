/**
 * Class: AuctionHouse
 * Description: Manages Interaction between agents and the ongoing auction.
 * @author Tanner Randall Hunt
 */
package auctionHouse;

import auctionHouse.auctionBlock.AuctionBlock;
import auctionHouse.auctionBlock.items.Item;
import auctionHouse.auctionBlock.items.ItemInfo;
import auctionHouse.proxies.AgentProxy;
import auctionHouse.proxies.BankProxy;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionHouse {

    private int auctionID = -1;
    private HashMap<Integer,AgentProxy> agentProxies = new HashMap<>();
    private AuctionBlock auctionBlock;
    private BankProxy bankProxy;
    private AuctionServer auctionServer;


    public AuctionHouse(String bankIP,int bankPort,int aucPort,String itemFile)
            throws IOException, InterruptedException {
        auctionServer = new AuctionServer(aucPort,this);
        this.auctionBlock = new AuctionBlock(this,itemFile);
        this.bankProxy = new BankProxy(bankIP,bankPort);
        System.out.println(new InetSocketAddress(InetAddress.getLocalHost(),aucPort));
        auctionID = bankProxy.register(new InetSocketAddress(InetAddress.getLocalHost(),aucPort));
        System.out.println("Auction ID: "+auctionID);

    }

    /**
     * Gets a list of itemInfos currently up for auction.
     * @return List of itemInfos
     */
    public List<ItemInfo> getItemsOnSale(){
        List<Item> items = new LinkedList<>(auctionBlock.values());
        List<ItemInfo> onSale = new LinkedList<>();
        for(Item item:items){
           onSale.add(new ItemInfo(item.getItemID(),item.getCurrentBid(),item.getTimeLeft()));
        }
        return onSale;
    }

    /**
     * Propose a bid on a specific item up for auction.
     * @param itemID Id of Item
     * @param agentID Bidding Agent ID
     * @param bidProposal Bid Amount proposed
     * @return true if bid is valid false if not.
     */
    public synchronized Item proposeBid(String itemID,int agentID,int bidProposal){
        Item item = auctionBlock.get(itemID);
        if(item!=null) {
            int currentBid = item.getCurrentBid();
            if (item.isOnSale() && bidProposal > currentBid) {
                if (bankProxy.queryBank(agentID, bidProposal)) {
                    int recentWinner = item.getCurrentWinner();
                    updateBidData(agentID, bidProposal, item);
                    if (recentWinner != -1) {
                        agentProxies.get(recentWinner).notifyItemOutBid(item);
                        bankProxy.queryBank(recentWinner, -currentBid);
                    }

                    updateItem(item);
                    return item;
                }
            }
        }
        return null;
    }
    //Updates bid the data on an item when a new valid bid is submitted.
    private synchronized void updateBidData(int agentID, int bidProposal, Item item) {
        item.setCurrentBid(bidProposal);
        item.setCurrentWinner(agentID);
        item.resetTimer();
    }

    //Notify All Agents that an item's data has changed
    private void updateItem(Item item){
        for(AgentProxy agentProxy:agentProxies.values()){
            agentProxy.notifyItemUpdate(item);
        }
    }

    /**
     * Notifies all agents that some time has passed on an item.
     * @param item item on which time has passed
     */
    public void notifyElapse(Item item){
        for(AgentProxy agentProxy:agentProxies.values()){
            agentProxy.notifyItemElapse(item);
        }
    }

    /**
     * Notifies agents and the bank in the event of manual auction shutdown.
     */
    public boolean endAuction() throws IOException {
        if(auctionBlock.shutDown()) {
            for (AgentProxy agentProxy : agentProxies.values()) {
                for (Item item : auctionBlock.values()) {
                    itemExpiration(item);
                }
                agentProxy.notifyAuctionEnd();
            }
            bankProxy.shutDown();
            auctionServer.shutDown();
            return true;
        }
        return false;
    }

    /**
     * Notify agents when an item has expired and also notifies winner.
     * @param item item which has expired.
     */
    public void itemExpiration(Item item){
        int winnerID = item.getCurrentWinner();
        for(AgentProxy agentProxy:agentProxies.values()){
            if(agentProxy.getAgentID() != winnerID)agentProxy.notifyItemExpiration(item);
            else if(agentProxy.getAgentID() == winnerID) {
                agentProxies.get(winnerID).notifyItemWin(item);
            }else {
                System.out.println("No Winner");
            }
        }

    }
    /**
     * Notify agents when a new item is available.
     * @param item new item up for auction.
     */
    public void newItem(Item item) {
        for(AgentProxy agentProxy:agentProxies.values()){
            agentProxy.notifyNewItem(item);
        }
    }

    /**
     * Registers a new Agent Proxy based on their bank ID.
     * @param agentProxy new Agent proxy
     */
    public void registerAgentProxy(AgentProxy agentProxy){
        agentProxies.put(agentProxy.getAgentID(),agentProxy);
        if(!auctionBlock.isStarted()&&agentProxies.size()>0){
            auctionBlock.startAuctionBlock();
        }
    }

    /**
     * Removes agent proxy in the event of disconnect.
     * @param agentID  agent proxy id to be removed
     */
    public void removeAgentProxy(int agentID){
        agentProxies.remove(agentID);
    }


    /**
     * Inner Class: AuctionServer
     * Description: Server that accepts and creates socket connections to agents.
     * @author Tanner Randall Hunt
     */
    public class AuctionServer extends ServerSocket implements Runnable, Serializable {
        boolean isRunning = true;
        private AuctionHouse auctionHouse;
        private Socket socket;
        private ExecutorService executor = Executors.newCachedThreadPool();

        public AuctionServer(int port,AuctionHouse auctionHouse)
                throws IOException, InterruptedException {
            super(port);
            this.auctionHouse = auctionHouse;
            new Thread(this).start();
        }

        @Override
        public void run() {

            while (isRunning){
                try {
                    socket = this.accept();
                    while(auctionID<0){
                            Thread.sleep(100);
                    }
                    socket.setKeepAlive(true);
                    System.out.println("New Agent Connection at: " + socket.getRemoteSocketAddress());
                    AgentProxy agentProxy = new AgentProxy(socket, auctionHouse, auctionID);
                    executor.execute(agentProxy);
                } catch (IOException | InterruptedException e) {
                    System.err.println("Auction Server Socket Closed on Read ");
                    try {
                        auctionHouse.endAuction();
                        break;
                    } catch (IOException ex) {
                        System.err.println("");
                    }
                }
            }
            System.out.println("Auction Server Shutdown");
        }


        /**
         * Shuts Down the Auction Server.
         * @throws IOException
         */
        public void shutDown( ) throws IOException {
            if(socket != null)socket.close();
            isRunning = false;
            this.close();
            executor.shutdownNow();
        }
    }









}
