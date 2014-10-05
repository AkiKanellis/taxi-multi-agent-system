package agent;

import world.Place;

/**
 * This class is used to describe the action results that an action can have.
 * ActionResult is a product produced from executing an Action.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class ActionResult {

    /**
     * Initialises an ActionResult properties
     *
     * @param succeeded True if Action was executed successfully, false if not
     * @param totalCost Total cost of the Action (Action cost + punishment cost
     * if applicable)
     * @param nextPlace The place the Agent will go after the Action is
     * completed
     */
    public ActionResult(final boolean succeeded, final int totalCost, final Place nextPlace) {
        _succeeded = succeeded;
        _totalCost = totalCost;
        _nextPlace = nextPlace;
    }

    /**
     * @return the action's success
     */
    public boolean isSucceeded() {
        return _succeeded;
    }

    /**
     * @return The total cost of the ActionResult
     */
    public int getTotalCost() {
        return _totalCost;
    }

    /**
     * @return Returns the next place the agent should be at because of the
     * action that was executed.
     */
    public Place getNextPlace() {
        return _nextPlace;
    }

    private final boolean _succeeded;
    private final int _totalCost;
    private final Place _nextPlace;
}
