/**
 * Class: ItemInfo
 * Description: Encapsulates read only data from an Item
 * @author Tanner Randall Hunt
 */
package auctionHouse.auctionBlock.items;

import java.io.Serializable;

public class ItemInfo extends ItemBase implements Serializable {

    /**
     * Creates and item with read only info extracted from an item.
     * @param itemID name of the Item
     * @param currentBid current highest bid
     * @param timeLeft time lefty
     */
    public ItemInfo(String itemID,int currentBid,int timeLeft) {
        this.itemID = itemID;
        this.currentBid = currentBid;
        this.timeLeft = timeLeft;
    }

    public  String toString(){
        return "Item ID: " +itemID+"\n"+"Current Bid: "+currentBid+"\n"+
                "Time Left: "+timeLeft+"\n";
    }

    /**
     * Overrides equals using the names of the items.
     * @param other the other ItemInfo
     * @return true if equal false if not
     */
    public boolean equals(ItemInfo other) {
        return this.itemID.equals(other.itemID);
    }
}
