package agent;

import message.MessageType;

/**
 * Bundles a MessageType and an integer into one object.
 */
public class AgentPair {

    private MessageType type;
    private int index;

    /**
     * Creates a new AgentPair with a type and an index.
     * @param type MessageType to set
     * @param index int index to set
     */
    public AgentPair(MessageType type, int index) {
        this.type = type;
        this.index = index;
    }

    /**
     * Returns the MessageType of the pair.
     * @return message type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns the index of the pair.
     * @return index
     */
    public int getIndex() {
        return index;
    }
}
