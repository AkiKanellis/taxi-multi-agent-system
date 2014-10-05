package agent;

import utilities.generalutils.Coordinates;
import world.Place;

/**
 * This class is used to describe the actions an Agent can take. Every time an
 * action is executed an ActionResult object is returned indicating if the
 * action was successful and the effect it had on the agent's belief.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Action {

    /**
     * Each Action has a name and a base cost.
     *
     * @param name The name of the action which determines what kind of action
     * it will be
     * @param agent The agent to perform the action on
     */
    public Action(final String name, final Agent agent) {
        if (!name.equals(EMBARK_NAME)
                && !name.equals(DISEMBARK_NAME)
                && !name.equals(UP_NAME)
                && !name.equals(DOWN_NAME)
                && !name.equals(LEFT_NAME)
                && !name.equals(RIGHT_NAME)) {
            throw new IllegalArgumentException("Invalid value for actionName: "
                    + name);
        }
        _name = name;
        _agent = agent;
    }

    /**
     * @return the name of the Action
     */
    public String getName() {
        return _name;
    }

    /**
     * Executes the correct action based on its name.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    public ActionResult run(final Place currentPlace) {
        switch (_name) {
            case EMBARK_NAME:
                return embark(currentPlace);
            case DISEMBARK_NAME:
                return disembark(currentPlace);
            case UP_NAME:
                return goUp(currentPlace);
            case DOWN_NAME:
                return goDown(currentPlace);
            case LEFT_NAME:
                return goLeft(currentPlace);
            case RIGHT_NAME:
                return goRight(currentPlace);
            default:
                throw new IllegalArgumentException("Invalid type for actionName: "
                        + _name);
        }
    }

    /**
     * Executes embark in order to load a client.
     * 
     * If the embark action was executed on a place where no available client 
     * exists then the agent gets a penalty of BASE_COST + PENALTY_COST, the
     * action has failed and the agent stays at the same place.
     * 
     * Otherwise the client is added to the agent's clientOnboard list and is then
     * removed from the place.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    private ActionResult embark(final Place currentPlace) {
        if (currentPlace.getFirstAvailableClient() == null) {
            return new ActionResult(false, BASE_COST + PENALTY_COST, currentPlace);
        }
        _agent.getBelief().addClientOnboard(currentPlace.getFirstAvailableClient());
        currentPlace.removeClient(currentPlace.getClientList().get(0));
        return new ActionResult(true, BASE_COST, currentPlace);
    }

    /**
     * Executes disembark in order to unload a client.
     * 
     * If the disembark action was not executed on the destination of the client
     * then the agent gets a penalty of BASE_COST + PENALTY_COST, the action has
     * failed and the agent stays at the same place.
     * 
     * Otherwise the client is removed from the agent's clientOnboard list, is
     * then removed from the world and the agent gets a reward of BASE_COST + REWARD_COST.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    private ActionResult disembark(final Place currentPlace) {
        if (!currentPlace.equals(_agent.getBelief().getClientsOnboard().get(0).getDestination())) {
            return new ActionResult(false, BASE_COST + PENALTY_COST, currentPlace);
        }
        currentPlace.removeClient(_agent.getBelief().getClientsOnboard().get(0));
        _agent.getBelief().removeClientOnboard(_agent.getBelief().getClientsOnboard().get(0));
        return new ActionResult(true, BASE_COST + REWARD_COST, currentPlace);
    }

    /**
     * Executes up in order to move up.
     * 
     * If the up action is making the agent go to a place that is out of bounds
     * of the world or on a place that has an opposing agent then the action fails
     * with a total cost of zero and the agent stays at the same place.
     * 
     * Otherwise the agent moves to the next location.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    private ActionResult goUp(final Place currentPlace) {
        final int newX = currentPlace.getCoords().getX() - 1;
        if (newX < 0) {
            return new ActionResult(false, 0, currentPlace);
        }

        final Place nextPlace = new Place(new Coordinates(newX, currentPlace.getCoords().getY())).getPlaceFromCollection(_agent.getPlaces());
        if (nextPlace.hasOpposingAgent(_agent)) {
            return new ActionResult(false, 0, currentPlace);
        }

        return new ActionResult(true, BASE_COST, nextPlace);
    }

    /**
     * Executes up in order to move down.
     * 
     * If the down action is making the agent go to a place that is out of bounds
     * of the world or on a place that has an opposing agent then the action fails
     * with a total cost of zero and the agent stays at the same place.
     * 
     * Otherwise the agent moves to the next location.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    private ActionResult goDown(final Place currentPlace) {
        final int newX = currentPlace.getCoords().getX() + 1;
        if (newX > 4) {
            return new ActionResult(false, 0, currentPlace);
        }

        final Place nextPlace = new Place(new Coordinates(newX, currentPlace.getCoords().getY())).getPlaceFromCollection(_agent.getPlaces());
        if (nextPlace.hasOpposingAgent(_agent)) {
            return new ActionResult(false, 0, currentPlace);
        }

        return new ActionResult(true, BASE_COST, nextPlace);
    }

    /**
     * Executes left in order to move left.
     * 
     * If the left action is making the agent go to a place that is out of bounds
     * of the world or on a place that has an opposing agent or on a place that 
     * has a wall in between then the action fails with a total cost of zero and
     * the agent stays at the same place.
     * 
     * Otherwise the agent moves to the next location.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    private ActionResult goLeft(final Place currentPlace) {
        final int newY = currentPlace.getCoords().getY() - 1;
        if (newY < 0) {
            return new ActionResult(false, 0, currentPlace);
        }

        Place nextPlace = new Place(new Coordinates(currentPlace.getCoords().getX(), newY)).getPlaceFromCollection(_agent.getPlaces());
        if (!isTraversable(currentPlace, nextPlace) || nextPlace.hasOpposingAgent(_agent)) {
            return new ActionResult(false, 0, currentPlace);
        }

        return new ActionResult(true, BASE_COST, nextPlace);
    }

    /**
     * Executes right in order to move right.
     * 
     * If the right action is making the agent go to a place that is out of bounds
     * of the world or on a place that has an opposing agent or on a place that 
     * has a wall in between then the action fails with a total cost of zero and
     * the agent stays at the same place.
     * 
     * Otherwise the agent moves to the next location.
     *
     * @param currentPlace The place from which the action will be executed
     * @return The result of the action
     */
    private ActionResult goRight(final Place currentPlace) {
        final int newY = currentPlace.getCoords().getY() + 1;
        if (newY > 4) {
            return new ActionResult(false, 0, currentPlace);
        }

        Place nextPlace = new Place(new Coordinates(currentPlace.getCoords().getX(), newY)).getPlaceFromCollection(_agent.getPlaces());
        if (!isTraversable(currentPlace, nextPlace) || nextPlace.hasOpposingAgent(_agent)) {
            return new ActionResult(false, 0, currentPlace);
        }

        return new ActionResult(true, BASE_COST, nextPlace);
    }

    /**
     * Checks if a traversable path exists from currentPlace to nextPlace
     *
     * @param currentPlace The current Place
     * @param nextPlace The next Place to go
     * @return True if there is no wall, false if not
     */
    private boolean isTraversable(final Place currentPlace, final Place nextPlace) {
        return currentPlace
                .getNonTraversablePlaces()
                .stream()
                .noneMatch((nonTraversablePlace) -> (nonTraversablePlace.equals(nextPlace)));
    }

    private final String _name;
    private Agent _agent;

    /**
     * Tuning parameters for the base cost of each action, the penalty cost if
     * the agent did an action that was not supposed to and the reward cost if
     * the agent did a disembark action.
     */
    private static final int BASE_COST = -1;
    private static final int PENALTY_COST = -10;
    private static final int REWARD_COST = 20;

    /**
     * Naming parameters for the names of the different actions.
     */
    public final static String EMBARK_NAME = "EMBARK";
    public final static String DISEMBARK_NAME = "DISEMBARK";
    public final static String UP_NAME = "UP";
    public final static String DOWN_NAME = "DOWN";
    public final static String LEFT_NAME = "LEFT";
    public final static String RIGHT_NAME = "RIGHT";
}
