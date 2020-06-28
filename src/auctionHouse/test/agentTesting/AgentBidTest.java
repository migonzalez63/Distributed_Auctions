package auctionHouse.test.agentTesting;

import auctionHouse.auctionBlock.items.ItemInfo;
import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class AgentBidTest implements Serializable,Runnable {
    private static int id;
    private  Socket client;

    private static HashMap<String,ItemInfo> items = new HashMap<>();
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    public AgentBidTest(String ip,int port,int id) throws IOException {
        this.id =id;
        client = new Socket(ip,port);
        out = new ObjectOutputStream(client.getOutputStream());
        in = new ObjectInputStream(client.getInputStream());
        new Thread(this).start();
        register();

    }

    private  void register() throws IOException {
        Message registration = new Message(MessageType.NEW_AGENT);
        registration.setAccountID1(id);
        writeMessage(registration);
    }


    @Override
    public void run() {
        while(true){
            try {
                Message message = (Message) in.readObject();
                processMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }
    private void processMessage(Message message){
        switch (message.getMessageType()){
            case ACCEPTED:{
                System.out.println("Bid on "+message.getItemID()+ " accepted");
                break;
            }
            case REJECTED:{
                System.out.println("Bid on "+message.getItemID()+ " rejected");
                break;
            }
            case ITEM_LIST:{
                for(ItemInfo itemInfo : message.getItemList()){
                    items.put(itemInfo.getItemID(),itemInfo);
                    System.out.println(itemInfo);
                }
                break;
            }
            case UPDATE:{
                System.out.println("ITEM UPDATE\n" + message.getItemInfo());
                items.remove(message.getItemInfo().getItemID());
                items.put(message.getItemInfo().getItemID(),message.getItemInfo());
                break;
            }

            case ELAPSED:{
               // System.out.println("ITEM ELAPSE\n" + message.getItemInfo());
                items.remove(message.getItemInfo().getItemID());
                items.put(message.getItemInfo().getItemID(),message.getItemInfo());
                break;
            }
            case EXPIRED:{
                System.out.println("ITEM EXPIRATION\n" + message.getItemInfo());
                items.remove(message.getItemInfo().getItemID());
                break;
            }
            case AUCTION_END:{
                System.out.println("AUCTION OVER");
                break;

            }
            case NEW_ITEM:{
                System.out.println("NEW ITEM\n" + message.getItemInfo());
                items.put(message.getItemInfo().getItemID(),message.getItemInfo());
                break;
            }
            case WIN:{
                System.out.println("ITEM WIN\n" + message.getItemInfo());
                items.remove(message.getItemInfo().getItemID(),message.getItemInfo());
                break;
            }
            case OUTBID:{
                System.out.println("OUTBID\n" + message.getItemInfo());
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
       new AgentBidTest(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("Select Item to bid on...");
           String input = scanner.nextLine();


                   ItemInfo itemInfo = items.get(input);
                   declareAmount(scanner, itemInfo);


       }
    }

    private static void declareAmount(Scanner scanner, ItemInfo itemInfo) throws IOException {
        String input;
        Message message;
        declareItem(itemInfo);
        input = scanner.nextLine();
        message = new Message(MessageType.NEW_BID);
        message.setAccountID1(id);
        message.setItemID(itemInfo.getItemID());
        message.setAmount(Integer.parseInt(input));
        writeMessage(message);
        return;
    }

    private static void declareItem(ItemInfo itemInfo) {
        System.out.println("Currently bidding on...\n" + itemInfo);
        System.out.println("Specify Amount to Bid...");
    }

    private static void writeMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }


}
