package agent.gui;

import auctionHouse.auctionBlock.items.ItemInfo;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A graphical object extending VBox and displaying all the information about
 * an Item.
 */
public class ItemBox extends VBox {

    //info
    private AuctionHouseTab parentTab;

    //visual components
    private Label name = new Label();
    private Label currentBid = new Label();
    private Label time = new Label();
    private Button bid = new Button("Bid");
    private Label currentlyHolding = new Label("Current High Bidder");

    /**
     * Creates a new ItemBox.
     * @param tab tab to reference
     * @param itemNumber number of item box
     */
    public ItemBox(AuctionHouseTab tab, int itemNumber) {
        parentTab = tab;
        bid.setDisable(true);
        bid.setOnAction(e -> parentTab.bidPopup(itemNumber));
        getChildren().addAll(name, currentBid, time, bid, currentlyHolding);
    }

    /**
     * Resets the ItemBox to display a new item.
     * @param item new item to display
     */
    public void newItem(ItemInfo item) {
        Platform.runLater(() -> {
            setName(item.getItemID());
            setCurrentBid(item.getCurrentBid());
            setTime(item.getTimeLeft());
            setCurrentlyHolding(false);
            bid.setDisable(false);
        });
    }

    /**
     * Sets the name displayed in the ItemBox.
     * @param name String name to display
     */
    public void setName(String name) {
        this.name.setText(name);
    }

    /**
     * Sets the current bid displayed.
     * @param bid int bid to display
     */
    public void setCurrentBid(int bid) {
        Platform.runLater(() -> {
            currentBid.setText(Integer.toString(bid));
        });
    }

    /**
     * Sets the remaining time displayed
     * @param time int time to display (seconds)
     */
    public void setTime(int time) {
        Platform.runLater(() -> {
            this.time.setText(Integer.toString(time));
        });
    }

    /**
     * Sets whether to display a label stating that the agent has the current
     * high bid on the item.
     * @param isHolding boolean true or false
     */
    public void setCurrentlyHolding(boolean isHolding) {
        currentlyHolding.setVisible(isHolding);
    }

    /**
     * Expires the item.
     */
    public void expire() {
        Platform.runLater(() -> {
            setName("No Item");
            setCurrentBid(0);
            setTime(0);
            setCurrentlyHolding(false);
            bid.setDisable(true);
        });
    }
}
