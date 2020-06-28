package Bank;

import message.Message;
import message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.List;

/**
 * Class BankOperator handles all messaging request sent from clients
 * connecting to bank server.
 */
public class BankOperator implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int id;

    private enum DialogPrompts {
        WELCOME, GOODBYE
    }

    /**
     * Given a socket, we will initialize the input and output streams to receive and
     * send messages and create a new ID.
     * @param socket
     * @throws IOException
     */
    public BankOperator(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        do {
            SecureRandom random = new SecureRandom();
            id = random.nextInt(100000);
        } while (Bank.containsMember(id) || id == 0);
    }

    /**
     * While the socket is not closed, we will read in messages from in stream and process
     * the message in order to create a reply to be sent through the out stream
     */
    @Override
    public void run() {
        Message input, output;

        do {
            try {
                input = (Message) in.readObject();

                output = processMessage(input);

                out.writeObject(output);
                out.flush();
            } catch (IOException | ClassNotFoundException e) {
                input = null;
                Bank.removeAuctionHouse(id);
                Bank.removeMember(id);
                Bank.removeAgent(id);
            }
        } while (input != null);

        /*
         * If the socket receives null, we will close our streams and sockets properly
         */
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add message to deal with deregistration
    /*
     * Updates the list of AgentProxy with a given message
     */
    private synchronized void updateList(MessageType message) {
        List<AgentProxy> agents = Bank.getAgents();

        for(AgentProxy agent: agents) {
            agent.putMessage(new Message(message));
        }
    }

    /*
     * Processes given message to give and sets proper reply to be sent out
     *
     * For NEW_AGENT: we create a new account, added to our bank, create a new
     *                AgentProxy, and send back an ID for their bank account along
     *                with a list of Auction House IP's and bank balance.
     *
     * For NEW_AUC: We create a new account, added to our bank, add it to our
     *              business members and notify the agent proxies about the
     *              new auction house created.
     *
     * For DEREGISTRATION: We remove a member from our bank and check to seee if
     *                     that member exist in our business member map or as an
     *                     agent proxy and remove it from those as well. If it exist
     *                     as a business member, we will notify our agents about it
     *                     with a new updated list.
     *
     * For QUERY: We get the bank account associated with the ID given to us and either
     *            check to see if the balance is greater than the hold amount requested.
     *            If it is, we place a hold on the account and send a confirmation. If the
     *            given amount is negative, we remove a hold from the account and return
     *            a confirmation
     *
     * For TRANSFER: Given two account ID, we will transfer money from one account to another.
     *               We will get rid of any associated holds in the first account, reduce its balance
     *               and add the new amount to the second account. We will then send an updated balance
     *               to whoever initiated the transfer.
     *
     *
     */
    private Message processMessage(Message message) {
        Message reply = null;
        BankAccount newAccount;

        switch(message.getMessageType()) {
            case NEW_AGENT:
                verboseDialog(DialogPrompts.WELCOME, id);
                newAccount = new BankAccount(id, message.getAmount());
                Bank.addMember(newAccount.getAccountID(), newAccount);
                AgentProxy newAgent = new AgentProxy(out, id);
                Bank.addAgentProxy(newAgent);
                new Thread(newAgent).start();

                reply = new Message(MessageType.REGISTRATION_CONFIRM);
                reply.setAccountID1(newAccount.getAccountID());
                reply.setAmount(newAccount.getBalance());
                reply.setAuctionList(Bank.getBusinessMembers());
                break;
            case NEW_AUC:
                verboseDialog(DialogPrompts.WELCOME, id);
                newAccount = new BankAccount(id, 0);
                Bank.addMember(newAccount.getAccountID(), newAccount);
                Bank.addBusinessMember(newAccount.getAccountID(), message.getInetAddress());
                updateList(MessageType.NEW_AUC);

                reply = new Message(MessageType.REGISTRATION_CONFIRM);
                reply.setAccountID1(newAccount.getAccountID());
                break;
            case DEREGISTRATION:
                verboseDialog(DialogPrompts.GOODBYE, message.getAccountID1());
                Bank.removeMember(message.getAccountID1());
                if(Bank.containsAuctionHouse(message.getAccountID1())) {
                    Bank.removeAuctionHouse(message.getAccountID1());
                    updateList(MessageType.DEREGISTRATION);
                } else {
                    Bank.removeAgent(message.getAccountID1());
                }
                break;
            case QUERY:
                int requestedID = message.getAccountID1();
                BankAccount requestedBankAccount = Bank.getMember(requestedID);

                reply = new Message(MessageType.QUERY_RESPONSE);
                reply.setAccountID1(message.getAccountID1());

                if(requestedBankAccount == null) {
                    reply.setFundsAvailable(false);
                    break;
                }

                if(message.getAmount() < 0) {
                    requestedBankAccount.removeHold(Math.abs(message.getAmount()));
                    reply.setFundsAvailable(true);
                } else if(requestedBankAccount.getBalance() >= message.getAmount()) {
                    requestedBankAccount.addHold(message.getAmount());
                    reply.setFundsAvailable(true);
                } else {
                    reply.setFundsAvailable(false);
                }

                break;
            case TRANSFER:
                int agentID = message.getAccountID1();
                int auctionHouseID = message.getAccountID2();

                BankAccount agentAccount = Bank.getMember(agentID);
                BankAccount auctionHouseAccount = Bank.getMember(auctionHouseID);

                agentAccount.removeHold(message.getAmount());
                agentAccount.changeBalance(-message.getAmount());

                auctionHouseAccount.changeBalance(message.getAmount());

                reply = new Message(MessageType.UPDATE_BALANCE);
                reply.setAmount(agentAccount.getBalance());
                break;
            case UPDATE_BALANCE:
                int accountID = message.getAccountID1();

                BankAccount requestedAccount = Bank.getMember(accountID);

                int availableFunds = requestedAccount.getBalance();

                reply = new Message(MessageType.UPDATE_BALANCE);
                reply.setAmount(availableFunds);
                break;
            case AUCTION_UPDATE:
                reply = new Message(MessageType.UPDATE_LIST);
                reply.setAuctionList(Bank.getBusinessMembers());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message.getMessageType());
        }
        return reply;
    }

    private void verboseDialog(DialogPrompts prompt, int id1) {
        switch (prompt) {
            case WELCOME:
                System.out.println("New member added to bank");
                System.out.println("Welcome " + id1);
                break;
            case GOODBYE:
                System.out.println("Member " + id1 + " deregistering from Bank");
                System.out.println("Thank you for choosing Generic Bank");
                System.out.println("Goodbye!");
                break;
        }
    }
}
