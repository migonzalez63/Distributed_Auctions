package agent.gui;

import agent.AgentManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Creates the graphical interface for the Agent program. Is also the main
 * entry point, and initializes the AgentManager to control the rest of the
 * program. Observes the AgentManager.
 */
public class AgentGUI extends Application implements Observer {

    // info
    private AgentManager manager;

    // visual components
    private BorderPane mainPane;
    private TabPane auctionHousePane;
    private VBox agentInfo;
    private Label id = new Label();
    private Label availableMoneyLabel = new Label();
    private Label heldMoneyLabel = new Label();
    private Button close = new Button("Close");

    // map for auction houses
    private Map<InetSocketAddress, AuctionHouseTab> auctionHouseMap
            = new HashMap<>();

    /**
     * Updates the GUI with the given argument. If the argument is an
     * InetSocketAddress, creates or removes an AuctionHouseTab to add to the
     * TabPane. If the argument is anything else, updates the money labels in
     * the GUI.
     * @param o observed object
     * @param arg InetSocketAddress or other
     */
    public void update(Observable o, Object arg) {
        AgentManager.debug("GUI update");
        if (arg instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress)arg;
            if (auctionHouseMap.containsKey(address)) {
                AuctionHouseTab toRemove = auctionHouseMap.remove(address);
                Platform.runLater(() -> {
                    auctionHousePane.getTabs().remove(toRemove);
                });
            } else {
                AuctionHouseTab newTab =
                        new AuctionHouseTab(manager.getProxy(address));
                manager.getProxy(address).addObserver(newTab);
                auctionHouseMap.put(address, newTab);
                Platform.runLater(() -> {
                    auctionHousePane.getTabs().add(newTab);
                });
                new Thread(manager.getProxy(address)).start();
            }
        } else {
            updateMoney();
        }
    }

    /**
     * Updates the money fields in the GUI.
     */
    public void updateMoney() {
        Platform.runLater(() -> {
            availableMoneyLabel.setText("Available Money: $" +
                    manager.getAvailableMoney());
            heldMoneyLabel.setText("Held Money: $" + manager.getHeldMoney());
        });
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

    /**
     * Starts the GUI and by extension the entire program.
     * @param primaryStage Stage to use
     */
    public void start(Stage primaryStage) {

        // obtain args and initialize manager
        Parameters params = getParameters();
        List<String> args = params.getRaw();
        Callable<AgentManager> callable = () -> new AgentManager(args, this);
        FutureTask<AgentManager> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        try {
            manager = futureTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // initialize gui
        mainPane = new BorderPane();
        mainPane.setMinSize(300, 300);
        auctionHousePane = new TabPane();
        agentInfo = new VBox();
        mainPane.setCenter(auctionHousePane);
        mainPane.setLeft(agentInfo);
        id.setText("Agent " + manager.id());

        // set close action
        close.setOnAction(event -> {
            if (manager.hasActiveBids()) {
                errorPopup("Cannot close when there are active bids.");
            } else {
                manager.close();
                primaryStage.close();
            }
        });

        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            close.fire();
        });

        agentInfo.getChildren().addAll(id, availableMoneyLabel, heldMoneyLabel,
                close);
        updateMoney();

        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                auctionHousePane.requestLayout();
            }
        };
        timer.start();
    }

    /**
     * Entry point of program. Command line arguments must be of the form
     * hostName, port, initialAmount.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
