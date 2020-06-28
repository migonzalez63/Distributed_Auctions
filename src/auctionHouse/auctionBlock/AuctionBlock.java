/**
 * Class: AuctionBlock
 * Description: Puts Items up for auction.
 * @author Tanner Randall Hunt
 */
package auctionHouse.auctionBlock;

import auctionHouse.AuctionHouse;
import auctionHouse.auctionBlock.items.Item;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AuctionBlock extends ConcurrentHashMap<String,Item>  implements Runnable {

    private boolean isRunning = true;
    private AuctionHouse auctionHouse;
    private boolean isStarted = false;
    private Deque<Item> itemList = new LinkedList<>();


    /**
     * Creates an AuctionBlock for Items to be put up for auction.
     * @param auctionHouse AuctionHouse
     */
    public AuctionBlock(AuctionHouse auctionHouse,String itemFile){
        this.auctionHouse = auctionHouse;
        initItemList(itemFile);
        for(int i = 0;i<3;i++){
            Item newItem = itemList.pop();
            this.put(newItem.getItemID(),newItem);
            System.out.println("New Item for sale:"+newItem);
        }
    }

    //Initialize Items from file
    private void initItemList(String itemFile){
        ItemFileReader itemFileReader = new ItemFileReader(itemFile);
        itemList = itemFileReader.getItems();
        for(Item item:itemList){
            System.out.println(item);
        }
    }

    //Runs the Auction.
    @Override
    public void run() {
        do {
            List<Item> removals = new LinkedList<>();
            for (Item item : this.values()) {
                int time = item.getTimeLeft();
                //every five seconds that elapse on an item notify elapse.
                if(time != 0 && time%5 == 0){
                    elapse(item);
                }
                //Item is expires remove it
                if (!item.elapseTimer()) {
                    removals.add(item);
                }

            }

            for (Item item : removals) {
                this.remove(item.getItemID());
                System.out.println(item + " Auction Expired");
                expire(item);
                if (!itemList.isEmpty()) {
                    Item newItem = itemList.pop();
                    newItem(newItem);
                    this.put(newItem.getItemID(),newItem);
                    System.out.println("New Item for sale:" + newItem);
                }else if(this.isEmpty()){
                    System.out.println("No More items for sale!");
                    try {
                        Thread.sleep(5000);
                        endAuction();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    isRunning = false;
                    break;
                }
            }

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException  e) {
                e.printStackTrace();
            }
        }while (isRunning);
        System.out.println("Auction Block ShutDown");
    }

    /**
     * Shut down the auction block
     * @return false if no item currently up has a bid, true if not.
     */
    public boolean shutDown() {
        boolean isCurrentlyBidding = false;
        for(Item current:this.values()){
            if(current.getCurrentBid() > 0){
                isCurrentlyBidding = true;
                break;
            }
        }
        if(!isCurrentlyBidding) {
            isRunning = false;
            return true;
        }
        return false;
    }

    /**
     * Start the AuctionBlock Thread
     */
    public void startAuctionBlock(){
        new Thread(this).start();
        isStarted = true;
    }

    /**
     * Get the start flag.
     * @return isStarted boolean representing if the AuctionBlock has started.
     */
    public boolean isStarted() {
        return isStarted;
    }

    //Ask the auction house to end the auction.
    private void endAuction()  {
        try {
            auctionHouse.endAuction();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Ask the auction house to notify agents of an item expiration.
    private void expire(Item item){
        auctionHouse.itemExpiration(item);
    }
    //Ask the auction house to notify agents of a new item up for auction.
    private void newItem(Item item){
        auctionHouse.newItem(item);
    }
    //Ask the auction house to notify agents of an item time elapse.
    private void elapse(Item item){
            auctionHouse.notifyElapse(item);
    }
}
