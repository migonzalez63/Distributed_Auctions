/**
 * Class: ItemFileReader
 * Description: File reader that reads a text file putting each line in a list.
 * @author Tanner Randall Hunt
 */
package auctionHouse.auctionBlock;

import auctionHouse.auctionBlock.items.Item;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Scanner;

public class ItemFileReader {

    private Deque<Item> items = new LinkedList<>();

    /**
     * Creates File reader that reads a text file putting each line in a list.
     * @param fileName name of text file to be read.
     */
    public ItemFileReader(String fileName){
        readInItems(fileName);
    }

    //Read in Item Names
    private void readInItems(String filename) {
        Scanner scanner = new Scanner(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(filename)));
        while(scanner.hasNext()){
            Item newItem = new Item(scanner.nextLine());
            items.push(newItem);
        }
    }

    /**
     * Gets list of Item names.
     * @return items List of item names.
     */
    public Deque<Item> getItems() {
        return items;
    }
}
