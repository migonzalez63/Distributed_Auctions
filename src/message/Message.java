package message;

import auctionHouse.auctionBlock.items.Item;
import auctionHouse.auctionBlock.items.ItemInfo;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

public class Message implements Serializable {

    private MessageType messageType;
    private int accountID1;
    private int accountID2;
    private String itemID;
    private ItemInfo itemInfo;
    private int amount;
    private boolean fundsAvailable;
    private InetSocketAddress socketAddress;
    private List<InetSocketAddress> auctionList = new LinkedList<>();
    private List<ItemInfo> itemList = new LinkedList<>();



    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getAccountID1() {
        return accountID1;
    }

    public void setAccountID1(int accountID1) {
        this.accountID1 = accountID1;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public List<InetSocketAddress> getAuctionList() {
        return auctionList;
    }

    public void setAuctionList(List<InetSocketAddress> auctionList) {
        this.auctionList = auctionList;
    }

    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(ItemInfo itemInfo) {
        this.itemInfo = itemInfo;
    }

    public List<ItemInfo> getItemList() {
        return itemList;
    }



    public int getAccountID2() {
        return accountID2;
    }

    public void setAccountID2(int accountID2) {
        this.accountID2 = accountID2;
    }

    public boolean isFundsAvailable() {
        return fundsAvailable;
    }

    public void setFundsAvailable(boolean fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public InetSocketAddress getInetAddress() {
        return socketAddress;
    }

    public void setInetAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public void setItemList(List<ItemInfo> itemList) {
        this.itemList = itemList;
    }
}