package Bank;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Class Bank acts as the main server for the Auctions. It will store its
 * members with a unique ID and store the auction houses in order to send
 * that information to the Agent. Provides key operations to handle members
 */
public class Bank {
    // Map of members with unique ID and BankAccount Object
    private static Map<Integer, BankAccount> members = Collections.synchronizedMap(new HashMap<>());

    // Map of Auction Houses with unique ID and their IP address
    private static Map<Integer, InetSocketAddress> auctionHouses = Collections.synchronizedMap(new HashMap<>());

    // List of proxy agents that will update agents when a new auction house is created
    private static List<AgentProxy> agents = new LinkedList<>();

    /**
     * Adds a member to our bank
     * @param id
     * @param account
     */
    public static void addMember(int id, BankAccount account) {
        members.put(id, account);
    }

    /**
     * Gets a member from our bank
     * @param accountID
     * @return BankAccount or null if it doesn't exist
     */
    public static BankAccount getMember(int accountID) {
        return members.get(accountID);
    }

    /**
     * Adds an Auction House to our Business Members
     * @param key
     * @param id
     */
    public static void addBusinessMember(int key, InetSocketAddress id) {
        auctionHouses.put(key, id);
    }

    /**
     * Adds an Agent Proxy to a list of clients that need to be updated
     * @param proxy
     */
    public static void addAgentProxy(AgentProxy proxy) {
        agents.add(proxy);
    }

    /**
     * Gets an Agent Proxy
     * @return AgentProxy or null if it doesn't exist
     */
    public static List<AgentProxy> getAgents() {
        return agents;
    }

    /**
     * Returns a List of IP Address for Auction Houses that are open
     * @return List of InetSocketAddress
     */
    public static List<InetSocketAddress> getBusinessMembers() {
        return new LinkedList<>(auctionHouses.values());
    }

    /**
     * Returns the number of current memebers registered to bank account
     * @return Integer indicating size of map
     */
    public static int getTotalMembers() {
        return members.size();
    }

    /**
     * Checks to see if a members currently is registered with the bank
     * @param key
     * @return
     */
    public static boolean containsMember(int key) {
        return members.containsKey(key);
    }

    /**
     * Checks to see if an Auction House exist within our business members
     * @param key
     * @return
     */
    public static boolean containsAuctionHouse(int key) {
        return auctionHouses.containsKey(key);
    }

    /**
     * Removes member from bank
     * @param key
     */
    public static void removeMember(int key) {
        members.remove(key);
    }

    /**
     * Removes Auction Houses from business members
     * @param key
     */
    public static void removeAuctionHouse(int key) {
        auctionHouses.remove(key);
    }

    /**
     * Removes Agent Proxy from update list
     * @param id
     */
    public static void removeAgent(int id) {
        AgentProxy requestedAgent = getAgentProxy(id);

        if(requestedAgent == null) {
            return;
        } else {
            agents.remove(requestedAgent);
        }
    }

    /**
     * Returns Agent Proxy in list
     * @param id
     * @return AgentProxy
     */
    private static AgentProxy getAgentProxy(int id) {
        for(AgentProxy agent: agents) {
            if(agent.getID() == id) {
                return agent;
            }
        }

        return null;
    }

    public static void main(String[] args) throws IOException {
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(portNumber);

        // Prints out IP address of server to be able to connect to it
        System.out.println("Welcome to Generic Bank.");
        System.out.println("IP Address: " + new InetSocketAddress(InetAddress.getLocalHost(), portNumber));

        Thread thread  = new Thread(() -> {
            while (true) {
                Scanner in = new Scanner(System.in);

                String input = in.nextLine().toUpperCase().trim();

                switch (input) {
                    case "MEMBERS":
                        System.out.println("Total Members: " + members.size());
                        Collection<BankAccount> memberAccounts = members.values();

                        for(BankAccount account : memberAccounts) {
                            System.out.println(account);
                        }
                        break;
                    case "AGENTS":
                        System.out.println("Checking Agents");
                        for(AgentProxy agent : agents) {
                            BankAccount agentMem = Bank.getMember(agent.getID());
                            System.out.println(agentMem);
                        }
                        break;
                    case "AUCTION HOUSES":
                        System.out.println("Checking Auction Houses");
                        Set<Integer> auctionMem = auctionHouses.keySet();

                        for(Integer auction : auctionMem) {
                            BankAccount auctionAcc = Bank.getMember(auction);
                            System.out.println(auctionAcc);
                        }
                        break;
                    case "SHUTDOWN":
                        System.out.println("Closing Bank.\nGoodbye");
                        System.exit(0);
                        break;
                    case "HELP":
                        System.out.println("MEMBERS: Checks the current members of the bank.");
                        System.out.println("AGENTS: Allows you to look at all active members that are agents.");
                        System.out.println("AUCTION HOUSES: Allows you to look at all active members that are auction houses.");
                        System.out.println("SHUTDOWN: Closes the bank");
                    default:
                        System.out.println("Do not recognize command.");
                        System.out.println("Use HELP to get a list of commands");
                }
            }
        });

        thread.start();

        /*
         * Waits for clients to connect then launches
         * a BankOperator thread to handle the client's request
         */
        while (true) {
            Socket clientSocket = serverSocket.accept();
            BankOperator operator = new BankOperator(clientSocket);
            new Thread(operator).start();
        }
    }
}
