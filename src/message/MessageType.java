package message;



/*
private MessageType messageType;
    private int accountID;
    private String itemID;
    private int bidAmount;
    private boolean fundsAvailable;
    private InetAddress inetAddress;
    private List<InetAddress> auctionList = new LinkedList<>();
    private List<Item> itemList = new LinkedList<>();

 */

public enum MessageType {
    //EXPECTED FIELDS
    NEW_BID,                // accountID1,itemID, amount
    ACCEPTED,               // accountID1,itemInfo, amount
    REJECTED,               // accountID1,itemInfo, amount
    OUTBID,                 // accountID1,itemInfo
    ITEM_LIST,              // accountID1,itemList(itemInfos)
    UPDATE,                 // accountID1,itemInfo
    EXPIRED,                // accountID1,itemInfo
    NEW_ITEM,               // accountID1,itemInfo
    ELAPSED,                // accountID1,itemInfo
    WIN,                    // accountID1,itemInfo
    AUCTION_END,            // accountID1
    QUERY,                  // accountID1, amount
    UPDATE_BALANCE,         // accountID1, amount?
    AUCTION_UPDATE,         // deprecated
    UPDATE_LIST,            // List of SocketAddresses
    TRANSFER,               // accountID1,accountID2,amount
    QUERY_RESPONSE,         // accountID1,fundsAvailable
    NEW_AUC,                // socketAddress
    NEW_AGENT,              // accountID1,amount
    DEREGISTRATION,         // accountID1
    //---------------------------------
    REGISTRATION_CONFIRM,   //  *Bank to Agent: accountID1,amount,List of SocketAddresses
    //  *Bank to Auction: accountID1
    //


    //maybe get_houses could be a string message in a general message type?
}
