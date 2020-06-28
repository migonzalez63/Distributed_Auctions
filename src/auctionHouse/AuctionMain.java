/**
 * Class: AuctionMain
 * Description: Main entry point for the auction house program.
 * @author Tanner Randall Hunt
 */
package auctionHouse;

import java.io.IOException;
import java.util.Scanner;

public class AuctionMain {

    /**
     * Main entry point for the Auction House Program
     * @param args Bank IP,Bank Port, AuctionHouse Port, item file
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        boolean isRunning = true;
        AuctionHouse auctionHouse = null;
        String bankIp = args[0];
        int bankPort = Integer.parseInt(args[1]);
        int auctionPort = Integer.parseInt(args[2]);
        String itemFile = args[3];
        try {
            auctionHouse = new AuctionHouse(bankIp,bankPort,auctionPort,itemFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Scanner scanner  = new Scanner(System.in);
        while(isRunning){
            String input = scanner.nextLine();
            switch(input){
                case "s":{
                    System.out.println("AUCTION SHUTDOWN");
                    if(auctionHouse != null){
                        if(auctionHouse.endAuction()) {
                            isRunning = false;
                        }else System.err.println("Current Bids Exist, can't shutdown");
                    }

                    break;
                }
                default:{
                    break;
                }
            }
        }

    }


}
