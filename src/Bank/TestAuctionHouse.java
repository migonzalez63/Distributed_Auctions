package Bank;

import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class TestAuctionHouse {
    private static boolean isActive = true;

    private static Message sendMessage(MessageType type, String input) {
        Message message = new Message(type);
        String[] args = input.split("\\s");

        for(String str: args) {
            System.out.println(str);
        }

        switch (type) {
            case QUERY:
                message.setAccountID1(Integer.parseInt(args[0]));
                message.setAmount(Integer.parseInt(args[1]));
                break;
            case UPDATE_BALANCE:
                message.setAccountID1(Integer.parseInt(args[0]));
                break;
            case DEREGISTRATION:
                message.setAccountID1(Integer.parseInt(input));
                break;
        }

        return message;
    }

    private static void parseMessage(Message message) {
        switch (message.getMessageType()) {
            case QUERY_RESPONSE:
                System.out.println("Account ID: " + message.getAccountID1());
                System.out.println("Verdict: " + message.isFundsAvailable());
                break;
            case UPDATE_BALANCE:
                System.out.println("Current Balance: " + message.getAmount());
                break;
            case DEREGISTRATION:
                System.out.println("Ending Auction House");
                isActive = false;

        //System.exit(0);
                break;
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().toString());
        try(Socket socket = new Socket(InetAddress.getLocalHost(), 44444);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            Message fromUser = new Message(MessageType.NEW_AUC);
            InetAddress address = socket.getInetAddress();
            int socketPort = socket.getLocalPort();
            System.out.println("Port: " + socketPort);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, socketPort);
            System.out.println("Inet Address: " + inetSocketAddress);
            fromUser.setInetAddress(inetSocketAddress);
            out.writeObject(fromUser);

            Message fromServer = (Message) in.readObject();

            System.out.println("Account ID: " + fromServer.getAccountID1());

            Scanner input = new Scanner(System.in);

            while(isActive) {
                Thread thread = new Thread(() -> {
                    Message fromUser1;
                    boolean stop = false;
                    System.out.println("\nEnter Action");
                    String stuff = input.nextLine().toUpperCase().trim();

                    System.out.println("Enter Arguments");

                    switch (stuff) {
                        case "QUERY":
                            String things = input.nextLine();
                            fromUser1 = sendMessage(MessageType.QUERY, things);
                            break;
                        case "CHECK BALANCE":
                            things = input.nextLine();
                            fromUser1 = sendMessage(MessageType.UPDATE_BALANCE, things);
                            break;
                        case "DEREGISTER":
                            things = input.nextLine();
                            fromUser1 = sendMessage(MessageType.DEREGISTRATION, things);
                            stop = true;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + stuff);
                    }

                    try {
                        out.writeObject(fromUser1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(stop) {
                        try {
                            in.close();
                            out.close();
                            System.exit(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

                fromServer = (Message) in.readObject();

                if(fromServer != null) {
                    parseMessage(fromServer);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ;
    }
}
