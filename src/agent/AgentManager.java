package agent;

import agent.gui.AgentGUI;
import agent.proxy.AuctionHouseProxy;
import agent.proxy.BankProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The main controller of the agent. Is responsible for initializing and
 * removing proxies and updating the GUI with those changes.
 */
public class AgentManager extends Observable {

    // debug print statements
    public static final boolean DEBUG = false;
    public static void debug(String string) {
        if (DEBUG) System.out.println(string);
    }

    // info
    private int id = 0;
    private int availableMoney;
    private int heldMoney = 0;

    // proxies
    private BankProxy bank;
    private Map<InetSocketAddress, AuctionHouseProxy> auctionHouseMap =
            new HashMap<>();

    // processing queues
    private BlockingQueue<Integer> IDQueue = new LinkedBlockingQueue<>();

    // gui
    private AgentGUI gui;

    /**
     * Constructs an AgentManager with the command line arguments.
     * @param args command line arguments
     */
    public AgentManager(List<String> args, AgentGUI gui) {
        // initialize BankProxy
        try {
            bank = new BankProxy(this, args.get(0),
                    Integer.valueOf(args.get(1)),
                    Integer.valueOf(args.get(2)));
        } catch (IOException e) {
            System.err.println("Bank not found");
            System.exit(-3);
        }
        new Thread(bank).start();

        // block until id is set
        try {
            id = IDQueue.take();
        } catch (InterruptedException e) {}

        this.gui = gui;
        addObserver(gui);

    }

    /**
     * Sets the ID of the agent.
     * @param id ID to set
     */
    public void setID(int id) {
        if (this.id == 0) {
            try {
                IDQueue.put(id);
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Returns the ID of the agent.
     * @return ID of agent
     */
    public int id() {
        return id;
    }

    /**
     * Returns the AuctionHouseProxy associated with a given address
     * @param address InetSocketAddress to match
     * @return matching AuctionHouseProxy
     */
    public AuctionHouseProxy getProxy(InetSocketAddress address) {
        return auctionHouseMap.get(address);
    }

    /**
     * Initializes connections to an auction house at the
     * given address and starts the corresponding proxy and tab.
     * @param toAdd address of auction house
     */
    private void addAuctionHouse(InetSocketAddress toAdd) {
        try {
            AuctionHouseProxy newProxy = new AuctionHouseProxy(this, toAdd);
            auctionHouseMap.put(toAdd, newProxy);
            setChanged();
            notifyObservers(toAdd);
            debug("There are " + countObservers() + " observers");
        } catch (IOException e) {
            System.err.println("Error connecting to Auction House at " + toAdd);
        }
    }

    /**
     * Removes an auction house from the agent.
     * @param toRemove address of auction house
     */
    private void removeAuctionHouse(InetSocketAddress toRemove) {
        auctionHouseMap.remove(toRemove);
        setChanged();
        notifyObservers(toRemove);
        debug("There are " + countObservers() + " observers");
    }

    /**
     * Updates the AuctionHouse list.
     * @param addresses updated list of addresses
     */
    public void updateAuctionList(List<InetSocketAddress> addresses) {
        List<InetSocketAddress> addList = new LinkedList<>();
        List<InetSocketAddress> removeList = new LinkedList<>();

        // find houses not in current list and add them
        for (InetSocketAddress address : addresses) {
            if (!auctionHouseMap.containsKey(address)) {
                addList.add(address);
            }
        }

        // find houses not in new list and remove them
        for (InetSocketAddress address : auctionHouseMap.keySet()) {
            if (!addresses.contains(address)) {
                removeList.add(address);
            }
        }

        // execute changes
        for (InetSocketAddress toAdd : addList) {
            addAuctionHouse(toAdd);
        }

        for (InetSocketAddress toRemove : removeList) {
            removeAuctionHouse(toRemove);
        }

        debug("Current Auction House List: " + auctionHouseMap.keySet().toString());
    }

    /**
     * Checks if there are any active bids in the agent.
     * @return true if active bid exist
     */
    public boolean hasActiveBids() {
        for (AuctionHouseProxy proxy : auctionHouseMap.values()) {
            if (proxy.hasActiveBids()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells the bank to transfer an amount of money to another account. Used
     * as a reference to the bank proxy by and auction house tab.
     * @param auctionID
     * @param amount
     */
    public void transfer(int auctionID, int amount) {
        bank.transfer(auctionID, amount);
    }

    /**
     * Sets the balance of the agent.
     * @param balance balance to set
     */
    public synchronized void setBalance(int balance) {
        if (availableMoney != balance && availableMoney != 0) {
            debug("MONEY ERROR");
        }
        availableMoney = balance;
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the available money of the agent.
     * @return available money
     */
    public synchronized int getAvailableMoney() {
        return availableMoney;
    }

    /**
     * Returns the money of the agent that is currently held in bids.
     * @return held money
     */
    public synchronized int getHeldMoney() {
        return heldMoney;
    }

    /**
     * Places or removes a hold on an amount of money. If the amount is
     * negative, removes a hold.
     * @param money amount of money to hold
     */
    public synchronized void hold(int money) {
        if (availableMoney >= money) {
            availableMoney -= money;
            heldMoney += money;
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Removes an amount of money from the held money.
     * @param money money to remove
     */
    public synchronized void pay(int money) {
        if (money <= heldMoney) {
            heldMoney -= money;
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Closes the Agent.
     */
    public void close() {
        bank.close();
        for (AuctionHouseProxy proxy : auctionHouseMap.values()) {
            proxy.close();
        }
    }

}
