package Bank;

import java.util.ArrayList;

/**
 * Class BankAccount handles all basic bank account operations, such as
 * adding and removing money from account, adding and removing holds, and
 * checking their balance. Each account will have its own unique id.
 */
public class BankAccount {
    private final int accountID;
    private int balance;
    private ArrayList<Integer> holds = new ArrayList<>();

    /**
     * Creates a BankAccount object with a unique bank account id and a
     * balance
     * @param accountID
     * @param balance
     */
    public BankAccount(int accountID, int balance) {
        this.accountID = accountID;
        this.balance = balance;
    }

    /**
     * Returns the account ID
     * @return Integer signifying account ID
     */
    public int getAccountID() {
        return accountID;
    }

    /**
     * Returns the available balance.
     * @return
     */
    public synchronized int getBalance() {
        int availableBalance = balance;

        /*
         * If no holds exist, then our available balance is our current balance.
         * Else we need to subtract the holds to get the available balance
         */
        if(holds.isEmpty()) {
            return availableBalance;
        } else {
            for(int hold: holds) {
                availableBalance -= hold;
            }
        }

        return availableBalance;
    }

    /**
     * Adds a hold to the account
     * @param amount
     */
    public synchronized void addHold(int amount) {
        holds.add(amount);
    }

    /**
     * Removes hold from the account
     * @param amount
     */
    public synchronized void removeHold(int amount) {
        if(!holds.isEmpty()) {
            holds.remove((Integer) amount);
        }
    }

    /**
     * Changes the balance of the account with the given
     * amount. Can add or subtract depending on the sign of
     * the amount
     * @param amount
     */
    public synchronized void changeBalance(int amount) {
        this.balance += amount;
    }

    @Override
    public String toString() {
        return "Bank ID: " + accountID + " Balance: " + getBalance();
    }
}
