package Bank;

import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestAgent {

    private static Message sendMessage(MessageType type, String input) {
        Message message = new Message(type);
        String[] args = input.split("\\s");

        for(String str: args) {
            System.out.println(str);
        }

        switch (type) {
            case TRANSFER:
                message.setAccountID1(Integer.parseInt(args[0]));
                message.setAccountID2(Integer.parseInt(args[1]));
                message.setAmount(Integer.parseInt(args[2]));
                break;
            case UPDATE_BALANCE:
                message.setAccountID1(Integer.parseInt(args[0]));
        }

        return message;
    }

    private static void parseMessage(Message message) {
        switch (message.getMessageType()) {
            case UPDATE_BALANCE:
                System.out.println("Current Balance: " + message.getAmount());
                break;
        }
    }
    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().toString());
        try(Socket socket = new Socket(InetAddress.getLocalHost(), 44444);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            Message fromUser = new Message(MessageType.NEW_AGENT);
            fromUser.setAmount(1000);
            out.writeObject(fromUser);

            Message fromServer = (Message) in.readObject();

            System.out.println("Account ID: " + fromServer.getAccountID1());
            System.out.println("Current Balance: " + fromServer.getAmount());
            System.out.println("Auction Houses: " + fromServer.getAuctionList());

            Scanner input = new Scanner(System.in);

            while(true) {
                System.out.println(fromServer.getMessageType());
                System.out.println(fromServer.getAuctionList());

                Thread thread = new Thread(() -> {
                    Message fromUser1;
                    System.out.println("\nEnter Action");
                    String stuff = input.nextLine().toUpperCase().trim();
                    System.out.println(stuff);

                    System.out.println("Enter Arguments");
                    switch (stuff) {
                        case "TRANSFER":
                            String things = input.nextLine();
                            fromUser1 = sendMessage(MessageType.TRANSFER, things);
                            break;
                        case "CHECK BALANCE":
                            things = input.nextLine();
                            fromUser1 = sendMessage(MessageType.UPDATE_BALANCE, things);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + stuff);
                    }

                    try {
                        out.writeObject(fromUser1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                thread.start();

                fromServer = (Message) in.readObject();

                parseMessage(fromServer);

            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
