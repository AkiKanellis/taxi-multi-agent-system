package agent;

import world.Place;

/**
 * This class holds the Intention of the agent or in other words his short term
 * goal for the completion of his final Intention (to get the highest score).
 * The first intention of the agent is set as his desire and afterwards he
 * completes smaller intentions in order to fulfil the bigger intention.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Intention {

    /**
     * The Intention has a name, a short term goal (a place to be at) and if it
     * is complete or not.
     *
     *
     * @param name Name of the Intention
     * @param shortTermGoal The short term goal to achieve
     */
    public Intention(final String name, final Place shortTermGoal) {
        if (!name.equals(GET_HIGHEST_SCORE_NAME)
                && !name.equals(FIND_CLIENT_NAME)
                && !name.equals(CALCULATE_PATH_NAME)
                && !name.equals(CALCULATE_NEXT_ACTION_NAME)
                && !name.equals(EXECUTE_NEXT_ACTION_NAME)) {
            throw new IllegalArgumentException("Invalid value for name: "
                    + name);
        }
        _name = name;
        _shortTermGoal = shortTermGoal;
        _isComplete = false;
    }

    /**
     *
     * @return the name of the intention
     */
    public String getName() {
        return _name;
    }

    /**
     *
     * @return the short term goal (the place to be at)
     */
    public Place getShortTermGoal() {
        return _shortTermGoal;
    }

    /**
     *
     * @param isComplete sets the intention to complete or not complete
     */
    public void setIsComplete(final boolean isComplete) {
        _isComplete = isComplete;
    }

    /**
     *
     * @return true if intention is complete, false if not
     */
    public boolean getIsComplete() {
        return _isComplete;
    }

    /**
     * Starts executing the appropriate actions based on the name of the
     * Intention
     *
     * @param agent The agent to complete his Intention.
     * @return true if the Intention was successfully completed, false if not.
     */
    public boolean run(final Agent agent) {
        switch (_name) {
            case FIND_CLIENT_NAME:
                return findClient(agent);
            case CALCULATE_PATH_NAME:
                return calculatePath(agent);
            case CALCULATE_NEXT_ACTION_NAME:
                return calculateNextAction(agent);
            case EXECUTE_NEXT_ACTION_NAME:
                return executeNextAction(agent);
            default:
                throw new IllegalArgumentException("Invalid value for name: "
                        + _name);
        }
    }

    /**
     * In order to find a client the agent has to roam to a random direction, so
     * he tries each basic movement until one of them works otherwise it returns
     * false.
     *
     * @param agent The agent to complete this Intention.
     * @return true if the Intention was successfully completed, false if not.
     */
    private boolean findClient(final Agent agent) {
        for (Action action : agent.getBasicMovementsActions()) {
            agent.setActions(action);
            if (agent.executeActions()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the path towards the short term goal using BFS and then checks
     * if the path that was found is valid or not.
     *
     * @param agent the agent to complete this Intention.
     * @return true if the Intention was successfully completed, false if not.
     */
    private boolean calculatePath(final Agent agent) {
        System.out.println(agent.getName()
                + " is calculating path to: "
                + agent.getIntention().getShortTermGoal().getFormattedCoords());
        agent.setPath(
                agent.getBelief().getCurrentPlace(),
                agent.getIntention().getShortTermGoal());
        if (!agent.getBelief().getCurrentPlace().equals(agent.getIntention().getShortTermGoal())
                && agent.getPathSize() <= 1) {
            return false;
        }
        return agent.isPathValid();
    }

    /**
     * Calculates the next appropriate action along the path and then checks if
     * the action list is valid or not.
     *
     * @param agent the agent to complete this Intention.
     * @return true if the Intention was successfully completed, false if not.
     */
    private boolean calculateNextAction(final Agent agent) {
        System.out.println(agent.getName()
                + " is calculating next action to: "
                + agent.getIntention().getShortTermGoal().getFormattedCoords());
        agent.setNextAction();
        return agent.isActionsValid();
    }

    /**
     * Executes the next appropriate action along the path and then checks if
     * the action is finished or not.
     *
     * @param agent the agent to complete this Intention.
     * @return true if the Intention was successfully completed, false if not.
     */
    private boolean executeNextAction(final Agent agent) {
        System.out.println(agent.getName()
                + " is executing action to: "
                + agent.getIntention().getShortTermGoal().getFormattedCoords());
        agent.executeActions();
        if (agent.isActionsFinished()) {
            return true;
        }
        return false;
    }

    private final String _name;
    private final Place _shortTermGoal;
    private boolean _isComplete;

    public static final String GET_HIGHEST_SCORE_NAME = "GET_HIGHEST_SCORE";
    public static final String FIND_CLIENT_NAME = "FIND_CLIENT";
    public static final String CALCULATE_PATH_NAME = "CALCULATE_PATH";
    public static final String CALCULATE_NEXT_ACTION_NAME = "CALCULATE_NEXT_ACTION";
    public static final String EXECUTE_NEXT_ACTION_NAME = "EXECUTE_NEXT_ACTION";
}
