package agent.gui;

import agent.AgentManager;
import agent.AgentPair;
import agent.proxy.AuctionHouseProxy;
import auctionHouse.auctionBlock.items.ItemInfo;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import message.MessageType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * An extension of Tab that implements Observer. Retrieves information for an
 * individual Auction House and displays it in ItemBoxes. Handles the visual
 * components of input messages. Observes an AuctionHouseProxy and updates
 * based on it.
 */
public class AuctionHouseTab extends Tab implements Observer {

    private AuctionHouseProxy auctionHouse;
    private HBox mainBox;
    private List<ItemBox> boxes = new LinkedList<>();

    /**
     * Initializes a new AuctionHouseTab with a given AuctionHouseProxy.
     * @param a AuctionHouseTab to reference
     */
    public AuctionHouseTab(AuctionHouseProxy a) {
        // initialize
        auctionHouse = a;
        mainBox = new HBox();
        initializeBoxes();
        setContent(mainBox);
    }

    /**
     * Updates the GUI based on the given argument.
     * @param o observable object
     * @param arg AuctionPair or ItemInfo
     */
    public synchronized void update(Observable o, Object arg) {
        // checks for AgentPair and runs based on MessageType given
        if (arg instanceof AgentPair) {
            AgentPair inputPair = (AgentPair) arg;
            int index = inputPair.getIndex();
            switch (inputPair.getType()) {
                case ITEM_LIST:
                    Platform.runLater(() -> {
                        setText(Integer.toString(auctionHouse.id()));
                    });
                    for (int i = 0; i < auctionHouse.numItems(); i++) {
                        if (i < 3) {
                            ItemBox box = (ItemBox) mainBox.getChildren().get(i);
                            box.newItem(auctionHouse.getItem(i));
                        }
                    }
                    break;
                case ELAPSED:
                    ItemBox elapsed = boxes.get(index);
                    elapsed.setTime(auctionHouse.getItem(index).getTimeLeft());
                    break;
                case UPDATE:
                    ItemBox update = (ItemBox) mainBox.getChildren().get(index);
                    ItemInfo updatedItem = auctionHouse.getItem(index);
                    update.setTime(updatedItem.getTimeLeft());
                    update.setCurrentBid(updatedItem.getCurrentBid());
                    break;
                case OUTBID:
                    ItemBox outbid = (ItemBox) mainBox.getChildren().get(index);
                    ItemInfo outbidItem = auctionHouse.getItem(index);
                    outbid.setCurrentlyHolding(false);
                    break;
                case EXPIRED:
                    ItemBox expired = (ItemBox) mainBox.getChildren().get(index);
                    expired.expire();
                    break;
                case NEW_ITEM:
                    ItemBox newItem = (ItemBox) mainBox.getChildren().get(index);
                    newItem.newItem(auctionHouse.getItem(index));
                    break;
                case ACCEPTED:
                    ItemBox accepted = (ItemBox) mainBox.getChildren().get(index);
                    accepted.setCurrentlyHolding(true);
                    break;
                case REJECTED:
                    Platform.runLater(() -> {
                        errorPopup("Bid rejected");
                    });
                    break;
                default:
                    break;
            }
        } else if (arg instanceof ItemInfo) {
            ItemInfo wonItem = (ItemInfo) arg;
            Platform.runLater(() -> {
                errorPopup("You won " + wonItem.getItemID() + " for $" +
                        wonItem.getCurrentBid());
            });
        }
    }

    /**
     * Initializes three ItemBoxes.
     */
    private void initializeBoxes() {
        for (int i = 0; i < 3; i++) {
            ItemBox newBox = new ItemBox(this, i);
            mainBox.getChildren().add(newBox);
            boxes.add(newBox);
        }
    }

    /**
     * Creates a popup window that asks the user to bid on an item. Takes the
     * bid and, if valid, tells the AuctionHouseProxy to bid.
     * @param i item index
     */
    public void bidPopup(int i) {
        // initialize window
        Stage popupWindow = new Stage();
        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Bid: Auction House " + auctionHouse.id());
        ItemInfo item = auctionHouse.getItem(i);

        // set up visual components
        Label itemID = new Label("Item: " + item.getItemID());
        Label currentBid = new Label("Current Bid: " +
                Integer.toString(item.getCurrentBid()));
        TextField bidField = new TextField();
        Button placeBid = new Button("Place Bid");
        Button cancel = new Button("Cancel Bid");
        VBox top = new VBox(itemID, currentBid);
        HBox bottom = new HBox(placeBid, cancel);
        BorderPane bp = new BorderPane(bidField);
        bp.setTop(top);
        bp.setBottom(bottom);

        // set button events
        placeBid.setOnAction(event -> {
            int toBid = 0;
            try {
                toBid = Integer.valueOf(bidField.getText());
                if (toBid > auctionHouse.getItem(i).getCurrentBid()) {
                    auctionHouse.placeBid(item.getItemID(), toBid);
                    popupWindow.close();
                } else {
                    errorPopup("Bid too low");
                }
            } catch (NumberFormatException nfe) {
                errorPopup("Invalid Bid");
            }
        });
        cancel.setOnAction(event -> popupWindow.close());

        popupWindow.setScene(new Scene(bp, 300, 150));
        popupWindow.showAndWait();
    }

    /**
     * Creates an error message popup with the given message.
     * @param errorMessage error message to display
     */
    private void errorPopup(String errorMessage) {
        // initialize window
        Stage errorPopup = new Stage();
        errorPopup.initModality(Modality.APPLICATION_MODAL);
        errorPopup.setTitle("Error");

        // set up visual components
        Label errorLabel = new Label(errorMessage);
        Button close = new Button("Close");
        close.setOnAction(closeEvent -> errorPopup.close());
        VBox errorBox = new VBox(errorLabel, close);

        // show and wait
        Scene errorScene = new Scene(errorBox, 200, 100);
        errorPopup.setScene(errorScene);
        errorPopup.showAndWait();
    }

}
