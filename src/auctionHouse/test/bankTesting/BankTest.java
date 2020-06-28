package auctionHouse.test.bankTesting;

import auctionHouse.AuctionHouse;
import auctionHouse.proxies.AgentProxy;
import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BankTest extends ServerSocket implements Serializable,Runnable {



    boolean isRunning = true;

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private static Message queryResponse;


    public BankTest(int port)
            throws IOException, InterruptedException {
        super(port);

        new Thread(this).start();
    }



    @Override
    public void run() {
        Executor executor = Executors.newCachedThreadPool();
        while (isRunning){
            try {
                Socket socket = this.accept();
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                while(true){
                    Message message = (Message) in.readObject();

                    processMessage(message);
                }

            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();
            }
        }
    }

    private void processMessage(Message message) throws IOException {
        switch(message.getMessageType()){
            case QUERY:{
                out.writeObject(queryResponse);
                out.flush();
                break;

            }
            case NEW_AUC:{
                 Message message1 = new Message(MessageType.REGISTRATION_CONFIRM);
                 message1.setAccountID1(10);
                out.writeObject(message1);
                out.flush();
                 break;
            }


        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new BankTest(Integer.parseInt(args[0]));
        queryResponse = new Message(MessageType.QUERY_RESPONSE);
        queryResponse.setAccountID1(21);
        queryResponse.setFundsAvailable(false);
        System.out.println("Current Response: " + queryResponse.isFundsAvailable());
        Scanner scanner = new Scanner(System.in);
        while(true){
            String input  = scanner.nextLine();
            if(input.equals("reject")){
                queryResponse = new Message(MessageType.QUERY_RESPONSE);
                queryResponse.setAccountID1(21);
                queryResponse.setFundsAvailable(false);
                System.out.println("Current Response: " + queryResponse.isFundsAvailable());
            }else if(input.equals("accept")) {
                queryResponse.setAccountID1(21);
                queryResponse.setFundsAvailable(true);
                System.out.println("Current Response: " + queryResponse.isFundsAvailable());
            }
        }

    }



}
