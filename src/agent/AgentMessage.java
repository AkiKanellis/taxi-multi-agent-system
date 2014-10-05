package agent;

import java.util.ArrayList;
import java.util.List;
import world.Place;

/**
 * This class is used for communication between two agents.
 *
 * The agent that reads the message acts accordingly to it.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class AgentMessage {

    /**
     * A message includes the recipient, sender, type and content.
     *
     * @param sender The sender of the message
     * @param recipient The recipient of the message
     * @param type The type of the message (predefined)
     * @param content The content of the message (a list of places)
     */
    public AgentMessage(final Agent sender, final Agent recipient, final String type, final List<Place> content) {
        if (!type.equals(CLIENT_LOCATION_TYPE)
                && !type.equals(ASSIST_IN_BLOCKING_TYPE)
                && !type.equals(CONFIRM_TYPE)
                && !type.equals(DENY_TYPE)) {
            throw new IllegalArgumentException("Invalid value for type: "
                    + type);
        }

        _sender = sender;
        _recipient = recipient;
        _type = type;
        _content = content;
    }

    /**
     * Works exactly like #AgentMessage(agent.Agent, agent.Agent,
     * java.lang.String, java.util.List) except this constructor is used for the
     * confirm and deny message where the type of the message speaks by itself
     * and no further actions are needed.
     *
     * @see #AgentMessage(agent.Agent, agent.Agent, java.lang.String,
     * java.util.List)
     */
    public AgentMessage(final Agent sender, final Agent recipient, final String type) {
        if (!type.equals(CLIENT_LOCATION_TYPE)
                && !type.equals(ASSIST_IN_BLOCKING_TYPE)
                && !type.equals(CONFIRM_TYPE)
                && !type.equals(DENY_TYPE)) {
            throw new IllegalArgumentException("Invalid value for type: "
                    + type);
        }

        _sender = sender;
        _recipient = recipient;
        _type = type;
        _content = new ArrayList<>();
    }

    /**
     *
     * @return this message's sender
     */
    public Agent getSender() {
        return _sender;
    }

    /**
     *
     * @return this message's type
     */
    public String getType() {
        return _type;
    }

    /**
     *
     * @return this message's content
     */
    public List<Place> getContent() {
        return _content;
    }

    /**
     * The recipient reads the sender's message and acts accordingly.
     *
     * If the action requested by the sender can be completed then the recipient
     * sends true indicating a "confirm" reply (which is handled in the Agent's
     * function).
     *
     * In case of a confirm or deny message acting on it is not needed and true
     * is the result always.
     *
     * @return true if confirm or deny or action can be completed, false if not.
     */
    public boolean readMessage() {
        System.out.print(_recipient.getName()
                + " is reading a message from " + _sender.getName() + " :"
                + "\n\tHeader: " + "[" + getType() + "]"
                + "\n\tContent: ");
        for (Place place : _content) {
            System.out.print(place.getFormattedCoords() + " ");
        }
        System.out.println("");

        switch (_type) {
            case CLIENT_LOCATION_TYPE:
                return addClientLocation();
            case ASSIST_IN_BLOCKING_TYPE:
                return assistInBlocking();
            case CONFIRM_TYPE:
                return true;
            case DENY_TYPE:
                return true;
            default:
                throw new IllegalArgumentException("Invalid type for message: "
                        + getType());
        }
    }

    /**
     * Adds the client list of the sender to the recipients client list.
     */
    private boolean addClientLocation() {
        for (Place place : _content) {
            _recipient.getBelief().addClientsLocation(place);
        }
        return true;
    }

    /**
     * In this case the sender asks the recipient to assist in blocking an
     * opponent therefore this message asks the recipient to enter competitive
     * mode.
     *
     * If the recipient is in either transferring mode or in cooldown then this
     * read will return false therefore the recipient will reply with deny.
     */
    private boolean assistInBlocking() {
        if (_recipient.getMode().equals(Agent.TRANSFERING_MODE)
                || _recipient.getCooldown() > 0) {
            return false;
        }
        _recipient.setMode(Agent.COMPETITIVE_MODE);
        return true;
    }

    private final Agent _sender;
    private final Agent _recipient;
    private final String _type;
    private final List<Place> _content;

    public static final String CLIENT_LOCATION_TYPE = "CLIENT_LOCATION";
    public static final String ASSIST_IN_BLOCKING_TYPE = "ASSIST_IN_BLOCKING";
    public static final String CONFIRM_TYPE = "CONFIRM";
    public static final String DENY_TYPE = "DENY";
}
