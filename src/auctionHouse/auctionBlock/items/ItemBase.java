/**
 * Class: ItemBase
 * Description: Superclass to Item and ItemInfo. Holds common fields between
 *              the two.
 * @author Tanner Randall Hunt
 *
 */
package auctionHouse.auctionBlock.items;

import java.io.Serializable;

public class ItemBase implements Serializable {

    protected int currentBid;
    protected String itemID;
    protected int timeLeft = 15;

    /**
     * Get the current high bid for item.
     * @return currentBid current high bid.
     */
    public int getCurrentBid() {
        return currentBid;
    }

    /**
     * Gets the Item ID.
     * @return itemID name of the item
     */
    public String getItemID() {
        return itemID;
    }

    /**
     * Gets the time left on the item.
     * @return time left till expiration
     */
    public int getTimeLeft() {
        return timeLeft;
    }

    /**
     * Outputs item id.
     * @return itemID.
     */
    public String toString(){
        return itemID;
    }


}
