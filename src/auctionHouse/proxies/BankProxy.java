/**
 * Class: BankProxy
 * Description: Handles communication with the bank.
 * @author Tanner Randall Hunt
 */
package auctionHouse.proxies;

import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class BankProxy{

    private int accountID;
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Creates a BankProxy, handling communication with the bank.
     * @param ipAddress ip address of the bank
     * @param port port of the bank
     */
    public BankProxy(String ipAddress, int port)   {
        pollConnection(ipAddress,port);
    }
    //Polls the bank every five seconds in the event
    // that the bank is not yet online.
    private void pollConnection(String ipAddress, int port){
        while(true){
            try {
                System.out.println("Polling for Bank...");
                client = new Socket(ipAddress,port);
                if(!client.isClosed()){
                    System.out.println("Bank Connection successful!");
                    out = new ObjectOutputStream(client.getOutputStream());
                    in = new ObjectInputStream(client.getInputStream());
                    break;
                }
            } catch (IOException  e) {
                System.err.println("Bank Connection Refused\n");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    /**
     * Sends Registration  message to the bank.
     * @param socketAddress address of the auction house
     * @return bank account number
     */
    public int register(InetSocketAddress socketAddress){
        Message message = new Message(MessageType.NEW_AUC);
        message.setInetAddress(socketAddress);
        Callable<Integer> callable = () -> {
            sendMessage(message);
            return processRegistration();
        };
        FutureTask<Integer> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        try {
            return futureTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
       sendMessage(message);
        return -1;
    }
    /**
     * Sends a message to the bank asking if a particular agent account can
     * acutely bid on an item.
     * @param accountID bank account of bidding agent
     * @param amount the dollar amount the agent is trying to bid.
     */
    public synchronized boolean queryBank(int accountID,int amount) {
        Message query = new Message(MessageType.QUERY);
        query.setAccountID1(accountID);
        query.setAmount(amount);
        Callable<Boolean> callable = () -> {
            sendMessage(query);
            return processQueryResponse();
        };
        FutureTask<Boolean> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        try {
            return futureTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Send Message over the socket.
    private void sendMessage(Message message){
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shuts down BankProxy deregistering with the bank.
     */
    public void shutDown()  {
        if(!client.isClosed()) {
            Message message = new Message(MessageType.DEREGISTRATION);
            message.setAccountID1(accountID);
            sendMessage(message);
            try {
                client.close();
                System.out.println("Bank Shutdown: Closing Socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Process the banks response the query.
    private boolean processQueryResponse(){
        try {
            Message message = (Message) in.readObject();
            if(message.getMessageType().equals(MessageType.QUERY_RESPONSE)){
                return message.isFundsAvailable();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Process the banks response to the registration
    private int processRegistration(){
        try {
            Message message = (Message) in.readObject();
            if(message.getMessageType().equals(MessageType.REGISTRATION_CONFIRM)){
                accountID = message.getAccountID1();
                return accountID;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }



}
