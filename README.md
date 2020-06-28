# Distributed Auctions

## Introduction

Distributed Auctions simulates a simple approach to an online auction system.
Using an agent as a bidder and an Auction House as an auctioneer, we are able to
communicate and bid on goods. Agents can have multiple Auction Houses while Auction
Houses can have multiple agents. A Bank is used as a middle man to handle transactions
and serves as a database to store Auction House information to allow Agents to connect
to them.

## Bank Server (Miguel)

### How to Use:
- In order to run the bank server, user must provide a port number when running the jar
  , i.e. BankServer.jar portNumber. (Note: I have found that ports of 50000 and above are usually 
  free and will not cause issues when trying to bind to them).
- Once the server is running, you can use certain commands:
    - MEMBERS: Allows you to look at all active members in the bank.
    - AGENTS: Allows you to look at all active members that are agents.
    - AUCTION HOUSES: Allows you to look at all active members that are auction houses.
    - SHUTDOWN: Closes the bank.
    - HELP: Provides a list of all commands and a description as to what they do.

### Documentation
Design documents and implementation details for BankServer can be found in the docs folder.

## Auction House (Tanner)

### How to Use:
- To run the Auction House the user must provide the host ip and port of a valid active 
  bank server, in addition the user must also provide the a valid port for the Auction
  House to run on and an item file name.
  
  i.e. AuctionHouse.jar bankHostIp bankPort auctionHousePort itemFileName.txt
  
  - There are two item files in the jar, they must be specified as follows.
    - items10.txt  
    - items100.txt 
   - items10.txt has 10 items.
   -  items100.txt has 100 items. 
    
  - If the jar is successfully run the auction will automatically begin when it has 
  connected to a bank and at least one agent has registered.
    - Note: You can manually shutdown the auction by typing "s" into the console.
### Behavior
  - Auction End Event
    - An auction will end when all items have expired. The user can also manually
    shutdown the auction by typing "s" in the console. In both cases the auction house 
    will un-register with the bank upon shut down. 
        - Note: The manual auction shutdown will not shut down the auction if there 
                are active bids.
  - Item Duration
    - Each Item will be up for auction for 15 seconds.
    - When an Item receives a bid the count down to expiration will be reset.
     
### Documentation
Design documents and implementation details for the Auction House can be found in 
the docs folder.



## Agent (Preston)

### How to Use:
- To run an agent, user must provide the host and port of a valid running bank 
  and an amount of money for the agent to start with.
- Once the program is running, all interaction will be done within the GUI.
- The agent id, available money, and held money is shown in the left panel.
- To change between auction houses, click the tabs along the top.
- To bid on an item, click one of the bid buttons and enter an amount of money 
  less than or equal to the amount of available money.
- To close the program, click either the close button in the left panel or the X
  to close the program. Both disconnect properly from the bank.

### Documentation
Design documents and implementation details for Agent can be found in the docs folder.

### Assumptions
It is assumed that an auction house will never sell more than three items.

### Debugging
There is a static debug method in the AgentManager class that prints out given statements when
the debug boolean is true.

