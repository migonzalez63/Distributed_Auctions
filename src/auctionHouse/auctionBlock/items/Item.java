/**
 * Class: Item
 * Description: An Item that is sold on the auction block.
 * @author Tanner Randall Hunt
 */
package auctionHouse.auctionBlock.items;

import java.io.Serializable;

public class Item extends ItemBase implements Serializable {

    private static final int DURATION = 15;
    private boolean onSale = true;
    private int currentWinner  = - 1;

    /**
     * Creates an item to be sold.
     * @param itemID name of the Item to sell.
     */
    public Item(String itemID){
        this.itemID = itemID;
    }

    /**
     * Gets the Item's Id number
     * @return itemID
     */
    public String getItemID() {
        return itemID;
    }

    /**
     * Gets on sale flag.
     * @return onSale
     */
    public synchronized boolean isOnSale() {
        return onSale;
    }

    private synchronized void expireItem() {
        this.onSale = false;
    }

    /**
     * Gets the Current Highest bid on the Item.
     * @return currentBid
     */
    public synchronized int getCurrentBid() {
        return currentBid;
    }

    /**
     * Sets the current highest bid.
     * @param currentBid Current Highest Bid
     */
    public synchronized void setCurrentBid(int currentBid) {
        this.currentBid = currentBid;
    }

    /**
     * Gets the current winners ID number.
     * @return currentWinner
     */
    public int getCurrentWinner() {
        return currentWinner;
    }

    /**
     * Sets the current winner.
     * @param currentWinner id number of Current winner
     */
    public synchronized void setCurrentWinner(int currentWinner) {
        this.currentWinner = currentWinner;
    }

    /**
     * Gets the amount of time left on this auction.
     * @return timeLeft
     */
    public int getTimeLeft() {
        return timeLeft;
    }

    /**
     * Decrease the time remaining on item by one.
     * @return returns true when the item still has time left,
     *          false if auction expires.
     */
    public synchronized boolean elapseTimer(){
        if(--timeLeft<= 0){
            expireItem();
            return false;
        }else return true;
    }

    /**
     * Resets the timer to the default time.
     */
    public synchronized void resetTimer(){
        timeLeft = DURATION;
    }
}
