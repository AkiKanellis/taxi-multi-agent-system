package agent;

/**
 * This class represents the Desire of the agent which is to get the highest
 * score. The desire is then converted to the appropriate intention for each
 * agent.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Desire {

    /**
     * The Desire of the agent holds the name of the Desire and if the Desire is
     * complete.
     *
     * @param name Name of the Desire
     */
    public Desire(final String name) {
        if (!name.equals("GET_HIGHEST_SCORE")) {
            throw new IllegalArgumentException("Invalid arguments for desire");
        }
        _name = name;
        _isComplete = false;
    }

    /**
     *
     * @return the name of the desire
     */
    public String getName() {
        return _name;
    }

    /**
     *
     * @param isComplete sets the desire to complete or not complete
     */
    public void setIsComplete(final boolean isComplete) {
        _isComplete = isComplete;
    }

    /**
     *
     * @return true if the desire is complete, false if not
     */
    public boolean getIsComplete() {
        return _isComplete;
    }

    private final String _name;
    private boolean _isComplete;
}
