package agent;

import utilities.generalutils.Helper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import world.Place;

/**
 * This class described the agent whom will plan and solve the problem in order
 * to achieve his desire.
 *
 * The agent is initialise with a desire which is then taken as an intention
 * which is then broken in separate intentions in order to achieve his final
 * goal.
 *
 * The agent's architecture consists of a Belief, a Desire and an Intention
 * based on the BDI model. The agent also holds the places in the world, the
 * path he has calculated to take, the list of actions he wishes to perform, the
 * score based on his efficiency, his team and id as well as a list of teamates
 * from which he can access their state in the world anytime. He also has a list
 * of messages which his teamates can access to send him a message.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Agent {

    /**
     * The constructor takes the team, id and the random object as parameters.
     *
     * Each agent is initialised in roam mode, with a score of zero, the
     * competitive counter and cooldown at zero as well as if he is to roam for
     * the next round as well at false. Also the basicMovementActions list is
     * initialised.
     *
     * @param team The agent's team
     * @param id The agent's id
     * @param rand The random object
     */
    public Agent(final String team, final int id, final Random rand) {
        _team = team;
        _id = id;

        _mode = "ROAM";
        _score = 0;
        _teamates = new ArrayList<>();
        _basicMovementActions = new ArrayList<>();
        _actionsToExecute = new ArrayList<>();
        _places = new ArrayList<>();
        _path = new ArrayList<>();
        _messages = new ArrayList<>();
        _rand = rand;

        _competitiveCounter = 0;
        _cooldown = 0;
        _roamNextRound = false;

        setBasicMovementsActions();
    }

    /**
     * @return The agent's team
     */
    public String getTeam() {
        return _team;
    }

    /**
     * @return The agent's id
     */
    public int getId() {
        return _id;
    }

    /**
     * @return The agent's formatted name (team + id)
     */
    public String getName() {
        return getTeam() + Integer.toString(getId());
    }

    /**
     * Sets the mode of the agent to one of the explicit existing modes and
     * prints the change (if any).
     *
     * @param mode The mode to set the agent at
     */
    public void setMode(String mode) {
        if (!mode.equals(TRANSFERING_MODE)
                && !mode.equals(KNOWS_CLIENT_LOCATION_MODE)
                && !mode.equals(ROAM_MODE)
                && !mode.equals(COMPETITIVE_MODE)
                && !mode.equals(OPPONENT_COOPERATIVE_MODE)) {
            throw new IllegalArgumentException("Invalid value for mode: "
                    + mode);
        }

        if (!_mode.equals(mode)) {
            Helper.modeChangePrint(getName(), _mode, mode);
        }

        _mode = mode;
    }

    /**
     * @return the agent's cooldown from the competitive mode
     */
    public int getCooldown() {
        return _cooldown;
    }

    /**
     * @return the list of teamates the agent has
     */
    public List<Agent> getTeamates() {
        return _teamates;
    }

    /**
     * @return the list of messages the agent has
     */
    public List<AgentMessage> getMessages() {
        return _messages;
    }

    /**
     * Adds a message to the agent's list of messages.
     *
     * @param message the message to add to the agent's list of messages
     */
    public void addMessage(AgentMessage message) {
        getMessages().add(message);
    }

    /**
     * Reads the messages sent to this agent and replies accordingly.
     *
     * If the message is a reply by itself then no reply is needed and the
     * reading continues.
     *
     * If the reading of the message returned true, indicating that the agent
     * could perform whatever was instructed for him then the agent will reply
     * with confirm, otherwise with a deny.
     *
     * After the agent is done reading the messages he clears his list since it
     * is no longer needed.
     */
    private void readMessages() {
        for (AgentMessage agentMessage : _messages) {
            if (agentMessage.getType().equals(AgentMessage.CONFIRM_TYPE)
                    || agentMessage.getType().equals(AgentMessage.DENY_TYPE)) {
                agentMessage.readMessage();
                continue;
            }

            if (agentMessage.readMessage()) {
                replyToMessage(agentMessage.getSender(), AgentMessage.CONFIRM_TYPE);
            } else {
                replyToMessage(agentMessage.getSender(), AgentMessage.DENY_TYPE);
            }
        }
        getMessages().clear();
    }

    /**
     * Replies to a message that was received with either confirm or deny and
     * prints the appropriate content.
     *
     * @param recipient The recipient of the confirm or deny message
     * @param type The type of the confirm or deny message (confirm or deny)
     */
    private void replyToMessage(final Agent recipient, final String type) {
        Helper.messageReplyPrint(getName(), recipient.getName(), type);

        recipient.addMessage(new AgentMessage(this, recipient, type));
    }

    /**
     * Sends a new message to the recipient specified and prints the appropriate
     * message.
     *
     * @param recipient The recipient of the message
     * @param type The type of the message
     * @param content A list of places as content (usually a list of clients
     * locations)
     */
    private void sendMessage(final Agent recipient, final String type, final List<Place> content) {
        Helper.messageSendPrint(getName(), recipient.getName(), type, content);

        recipient.addMessage(new AgentMessage(this, recipient, type, content));
    }

    /**
     * Works just like sendMessage(Agent,String,List<Place>) except this one is
     * used in case the type of the message is enough of the recipient to
     * compute the message.
     *
     * @see #sendMessage(Agent,String,List<Place>)
     */
    private void sendMessage(final Agent recipient, final String type) {
        Helper.messageSendPrint(getName(), recipient.getName(), type);

        recipient.addMessage(new AgentMessage(this, recipient, type));
    }

    /**
     * This method is used to send the appropriate messages depending on the
     * belief and the mode of the Agent.
     *
     * Exchange client position: For each teamate the agent checks to see if the
     * teamate is at the same block as the agent. If he is then the agent will
     * send him a message with all the client positions that he knows. But that
     * has as a consequence for the message to be also heard in all the
     * neighbouring Places (visiblePlaces from the current place). So for each
     * visible place (except the same Place) the agent also sends the message to
     * any agent that is there currently.
     *
     * Ask for assistance: If the agent is in competitive mode and the agent has
     * an enemy in his line of sight and a teamate has the same enemy in his
     * line of sight then the agent will send to that teamate an assistance to
     * block message in order for the teamate to enter competitive mode and help
     * in blocking the enemy efficiently.
     */
    private void sendMessages() {
        // Exchange client position
        for (Agent teamate : _teamates) {
            if (teamate.getBelief().getCurrentPlace().equals(getBelief().getCurrentPlace())
                    && getBelief().knowsClientLocation()) {
                sendMessage(teamate, AgentMessage.CLIENT_LOCATION_TYPE, getBelief().getClientsLocation());
                for (Place neighborPlace : getBelief().getVisiblePlaces()) {
                    if (!neighborPlace.equals(getBelief().getCurrentPlace())) {
                        for (Agent agent : neighborPlace.getAgentList()) {
                            sendMessage(agent, AgentMessage.CLIENT_LOCATION_TYPE, getBelief().getClientsLocation());
                        }
                    }
                }
            }

            // Ask for assistance
            if (_mode.equals(COMPETITIVE_MODE)) {
                for (Place visiblePlace : getBelief().getVisiblePlaces()) {
                    if (visiblePlace.hasOpposingAgent(this)) {
                        for (Place teamateVisiblePlace : getTeamates().get(0).getBelief().getVisiblePlaces()) {
                            if (teamateVisiblePlace.equals(visiblePlace)) {
                                sendMessage(teamate, AgentMessage.ASSIST_IN_BLOCKING_TYPE);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a teamate to the teamates list as long as the teamate is not equal
     * to the current agent.
     *
     * @param teamate The teamate to add
     */
    public void addTeamate(Agent teamate) {
        if (!teamate.equals(this)) {
            _teamates.add(teamate);
        }
    }

    /**
     * This is where the Agent's levels of priority are set.
     *
     * Highest priority has the case of the Agent transferring a client.
     *
     * Now depending on the agent:
     *
     * If he is a Rooster and there is an opposing agent nearby, then the agent
     * will enter the competitive mode as a second priority level in order to
     * obstruct him, as a third priority level if he knows the location of a
     * client he will enter the knows client location mode in order to seek him
     * and as a last priority if he can do none of the above he is to roam
     * around until he can do something.
     *
     * If he is a Donkey then the only change is that as a second priority he
     * has 70% chance to be competitive towards the enemy agent he saw and a 30%
     * chance to cooperate with the opposing agent by giving him information of
     * clients locations.
     */
    private void determineMode() {
        // Level 1
        if (_mode.equals(TRANSFERING_MODE)) {
            return;
        }

        switch (_team) {
            case "R":
                // Level 2
                if (getBelief().opponentNearby() && _cooldown == 0) {
                    setMode(COMPETITIVE_MODE);
                } else if (getBelief().knowsClientLocation()) { // Level 3
                    setMode(KNOWS_CLIENT_LOCATION_MODE);
                }
                break;
            case "D":
                final int randInt = _rand.nextInt(101);

                if (getBelief().opponentNearby()
                        && _cooldown == 0
                        && (Helper.isBetween(randInt, 0, 70) || _mode.equals(COMPETITIVE_MODE))) { // Level 2
                    setMode(COMPETITIVE_MODE);
                } else if (getBelief().opponentNearby()
                        && !_mode.equals(COMPETITIVE_MODE)
                        && Helper.isBetween(randInt, 71, 100)) { // Level 2
                    setMode(OPPONENT_COOPERATIVE_MODE);
                } else if (getBelief().knowsClientLocation()) { // Level 3
                    setMode(KNOWS_CLIENT_LOCATION_MODE);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid arguments for team: " + _team);
        }
    }

    /**
     * @return the current mode of the agent
     */
    public String getMode() {
        return _mode;
    }

    /**
     * @return the formatted mode for use in the GUI
     */
    public String modeToString() {
        switch (_mode) {
            case ROAM_MODE:
                return ";";
            case KNOWS_CLIENT_LOCATION_MODE:
                return "!";
            case TRANSFERING_MODE:
                return "";
            case COMPETITIVE_MODE:
                return "-";
            default:
                throw new IllegalArgumentException("Invalid value for mode: " + _mode);
        }
    }

    /**
     * @param belief the belief to set
     */
    public void setBelief(final Belief belief) {
        _belief = belief;
    }

    /**
     * This method updates the belief of the agent.
     *
     * Firstly it clears the agent's known opponents and teamates locations,
     * secondly adds the clients locations that he can see nearby, thirdly adds
     * the new opponents locations and lastly the new teamates locations.
     */
    public void determineBelief() {
        _belief.getOpponentsLocation().clear();
        _belief.getTeamatesLocation().clear();

        _belief.addClientsLocation(getNearbyAvailableClients());

        for (Place place : nearbyOpponentsLocation()) {
            _belief.addOpponentLocation(place);
        }
        for (Agent teamate : _teamates) {
            _belief.addTeamatesLocation(teamate.getBelief().getCurrentPlace());
        }
    }

    /**
     * @return the current belief of the agent
     */
    public Belief getBelief() {
        return _belief;
    }

    /**
     * @param intention The intention to set
     */
    public void setIntention(final Intention intention) {
        _intention = intention;
    }

    /**
     * @return the current intention of the agent
     */
    public Intention getIntention() {
        return _intention;
    }

    /**
     * @param desire the desire to set
     */
    public void setDesire(final Desire desire) {
        _desire = desire;
    }

    /**
     * @return the current desire of the agent
     */
    public Desire getDesire() {
        return _desire;
    }

    /**
     * @return the current score of the agent
     */
    public int getScore() {
        return _score;
    }

    /**
     * Adds the 3 basic movements actions to the basic movements list.
     */
    private void setBasicMovementsActions() {
        _basicMovementActions.add(new Action(Action.UP_NAME, this));
        _basicMovementActions.add(new Action(Action.DOWN_NAME, this));
        _basicMovementActions.add(new Action(Action.LEFT_NAME, this));
        _basicMovementActions.add(new Action(Action.RIGHT_NAME, this));
    }

    /**
     * @return the basic movement actions list
     */
    public List<Action> getBasicMovementsActions() {
        return _basicMovementActions;
    }

    /**
     * Sets the places of the world to the agent places list.
     *
     * @param places the places of the world
     */
    public void setPlaces(final List<Place> places) {
        for (Place place : places) {
            _places.add(place);
        }
    }

    /**
     * @return the agent's places
     */
    public List<Place> getPlaces() {
        return _places;
    }

    /**
     * Gets the clients locations that are in line-of-sight of the Agent
     *
     * @return list of clients locations
     */
    public List<Place> getNearbyAvailableClients() {
        List<Place> clientsLocation = new ArrayList<>();
        for (Place place : _belief.getVisiblePlaces()) {
            if (place.getFirstAvailableClient() != null) {
                clientsLocation.add(place);
            }
        }
        return clientsLocation;
    }

    /**
     * Gets the opponent locations that are in line-of-sight of the Agent
     *
     * @return list of opponent locations
     */
    public List<Place> nearbyOpponentsLocation() {
        List<Place> opponentsLocation = new ArrayList<>();
        for (Place place : _belief.getVisiblePlaces()) {
            if (place.hasAgent()) {
                for (Agent agent : place.getAgentList()) {
                    if (!agent.getTeam().equals(_team)) {
                        opponentsLocation.add(place);
                    }
                }
            }

        }
        return opponentsLocation;
    }

    /**
     * Sets the best path to the current destination (client's position,
     * opponent's flanking position or final destination) by calling the
     * breadthFirstSearch function.
     *
     * @param start The starting Place
     * @param end The ending Place
     */
    public void setPath(final Place start, final Place end) {
        _path = breadthFirstSearch(start, end);
    }

    /**
     * After the breadthFirstSearch function has finished, getPath starts from
     * the end Place and goes upwards following each parent until it reaches the
     * start Place. Then it clears the parents that were set and reverses the
     * path that was found and returns it.
     *
     * @param end The goal that we wanted to reach with breadthFirstSearch
     * @return The path from start to end
     */
    private List<Place> getPath(final Place end) {
        final List<Place> reversedPath = new ArrayList<>();
        for (Place place = end; place != null; place = place.getParent()) {
            reversedPath.add(place);
        }

        clearParents();

        final List<Place> path = new ArrayList<>();
        ListIterator li = reversedPath.listIterator(reversedPath.size());
        while (li.hasPrevious()) {
            path.add((Place) li.previous());
        }
        return path;
    }

    /**
     * @return the size of the path list
     */
    public int getPathSize() {
        return _path.size();
    }

    /**
     * Checks if a path is valid or not.
     *
     * @return true if path is valid, false if not
     */
    public boolean isPathValid() {
        return !_path.isEmpty();
    }

    /**
     * Checks if the action list is valid or not.
     *
     * @return true if the action list is valid, false if not
     */
    public boolean isActionsValid() {
        return _actionsToExecute.size() > 0;
    }

    /**
     * Checks if the action list is empty meaning no more actions need to be
     * performed.
     *
     * @return true if the action list is empty, false if not
     */
    public boolean isActionsFinished() {
        return _actionsToExecute.isEmpty();
    }

    /**
     * Initialises the current state of the Agent with the current Belief and
     * the current Desire. The Desire then becomes the Intention of the Agent.
     *
     * @param belief The current belief of the Agent
     * @param desire The desire of the Agent
     */
    public void initializeState(final Belief belief, final Desire desire) {
        if (belief == null || desire == null) {
            throw new IllegalArgumentException("Invalid values for initial state");
        }
        _belief = belief;
        _desire = desire;
        _intention = new Intention(desire.getName(), null);
    }

    /**
     * go is the core function of the Agent.
     *
     * The Agent determines his belief, reads his messages (and replies to
     * them), removes the redundant clients that he thought that existed but do
     * not exist anymore, sends messages to his teamates, determines the mode he
     * needs to be in, performs the actions that need to be performed based on
     * his mode and in the end he reloads the places that are in his
     * line-of-sight.
     */
    public void go() {
        if (_cooldown > 0) {
            _cooldown--;
        }
        determineBelief();
        readMessages();
        getBelief().removeRedundantClients();
        sendMessages();
        determineMode();
        switch (_mode) {
            case ROAM_MODE:
                roamActions();
                break;
            case KNOWS_CLIENT_LOCATION_MODE:
                knowsClientLocationActions();
                break;
            case TRANSFERING_MODE:
                transferingActions();
                break;
            case COMPETITIVE_MODE:
                competitiveActions();
                break;
            case OPPONENT_COOPERATIVE_MODE:
                opponentCooperativeActions();
                break;
            default:
                throw new IllegalArgumentException("Invalid value for mode: "
                        + _mode);
        }

        _belief.setVisiblePlaces(_places);
        System.out.println();
    }

    /**
     * These actions are only executed in roam mode.
     *
     * The agent tries to go to a random direction (if possible).
     */
    public void roamActions() {
        List<Intention> intentions = plan(null);
        Collections.shuffle(getBasicMovementsActions(), _rand);
        runIntentions(intentions);
    }

    /**
     * These actions are only executed in knows client location mode.
     *
     * The agent will try to get to the closest client location that he is aware
     * of by creating a map based on distance from his location to the clients
     * location. Then the agent plans his intentions, calculates the path, next
     * action and executes that action in order to get to the clients location.
     *
     * If the agent either can't run an intention needed for the closest client
     * location he then will try the next closest client location until he has
     * no more.
     *
     * If he runs out of client locations he then resets to ROAM_MODE and goes
     * to a random direction (if possible).
     */
    public void knowsClientLocationActions() {
        HashMap<Place, Double> clientLocations = new HashMap<>();
        for (Place clientLocation : getBelief().getClientsLocation()) {
            clientLocations.put(clientLocation, Helper.getDistance(getBelief().getCurrentPlace(), clientLocation));
        }

        Map<Place, Double> sortedClientLocations = Helper.sortByValues(clientLocations);

        List<Intention> intentions = new ArrayList<>();
        for (Map.Entry<Place, Double> entry : sortedClientLocations.entrySet()) {
            Place closestClientLocation = entry.getKey();

            intentions.addAll(plan(closestClientLocation));
            if (runIntentions(intentions)) {
                Helper.foundPathPrint(getName(), closestClientLocation.getFormattedCoords(), closestClientLocation.getState());
                Helper.movedToPrint(getName(), getBelief().getCurrentPlace().getFormattedCoords(), getBelief().getCurrentPlace().getState());
                intentions.clear();
                return;
            } else {
                Helper.didNotFindPathPrint(getName(), closestClientLocation.getFormattedCoords(), closestClientLocation.getState());
                Helper.didNotMoveToPrint(getName(), closestClientLocation.getFormattedCoords(), closestClientLocation.getState());
                intentions.clear();
            }
        }
        setMode(ROAM_MODE);
        roamActions();
    }

    /**
     * These actions are only executed in transferring mode.
     *
     * The agent will try to get to the client's destination. Then the agent
     * plans his intentions, calculates the path, next action and executes that
     * action in order to get to the clients destination.
     *
     * If the agent either can't run an intention needed for the client's
     * destination he then resets to ROAM_MODE, goes to a random direction, goes
     * back to transferring mode and sets the flag to true in order to roam
     * again the next round. So in total he will roam for two rounds (if
     * possible).
     */
    public void transferingActions() {
        if (_roamNextRound) {
            setMode(ROAM_MODE);
            roamActions();
            setMode(TRANSFERING_MODE);
            _roamNextRound = false;
            return;
        }
        List<Intention> intentions = new ArrayList<>();
        Place clientDestination = getBelief().getClientsOnboard().get(0).getDestination();

        intentions.addAll(plan(clientDestination));
        if (runIntentions(intentions)) {
            System.out.println(getName() + " found path for " + clientDestination.getFormattedCoords()
                    + " (" + clientDestination.getState() + ")");

            System.out.println(getName() + " moved to "
                    + _belief.getCurrentPlace().getFormattedCoords());
            intentions.clear();
            return;
        } else {
            intentions.clear();
            System.out.println(getName() + " could not find path for " + clientDestination.getFormattedCoords()
                    + " (" + clientDestination.getState() + ")");
            System.out.println(getName() + " could not move to " + clientDestination.getFormattedCoords()
                    + " (" + clientDestination.getState() + ")");
        }

        setMode(ROAM_MODE);
        roamActions();
        setMode(TRANSFERING_MODE);
        _roamNextRound = true;
    }

    /**
     * The agent will try to get to the closest flanking location that he is
     * aware of by creating a map based on distance from his location to the
     * opponents surrounding (flanking) location. Then the agent plans his
     * intentions, calculates the path, next action and executes that action in
     * order to get to the flanking location. If he already is to such a
     * location then he is going to stay there for the round.
     *
     * If the agent either can't execute that action or can't find an accessible
     * flanking location from his location he then resets to ROAM_MODE and goes
     * to a random direction (if possible).
     *
     *
     * If he exceeded the number of rounds that he can stay in COMPETITIVE_MODE
     * he then resets to ROAM_MODE.
     */
    public void competitiveActions() {
        HashMap<Place, Double> flankingLocations = new HashMap<>();
        for (Place flankingLocation : getFlankingLocations()) {
            if (!flankingLocation.hasTeamateAgent(this)) {
                flankingLocations.put(flankingLocation, Helper.getDistance(_belief.getCurrentPlace(), flankingLocation));
            }
        }
        Map<Place, Double> sortedFlankingLocations = Helper.sortByValues(flankingLocations);

        List<Intention> intentions = new ArrayList<>();
        for (Map.Entry<Place, Double> entry : sortedFlankingLocations.entrySet()) {
            if (_competitiveCounter >= MAX_COMPETITIVE_ROUNDS) {
                break;
            }
            Place closestFlankingLocation = entry.getKey();

            if (closestFlankingLocation.equals(getBelief().getCurrentPlace())) {
                _competitiveCounter++;
                System.out.println(getName() + " stayed at " + closestFlankingLocation.getFormattedCoords()
                        + " (" + closestFlankingLocation.getState() + ")");
                return;
            }

            intentions.addAll(plan(closestFlankingLocation));
            if (runIntentions(intentions)) {
                _competitiveCounter++;
                System.out.println(getName() + " found path for " + closestFlankingLocation.getFormattedCoords()
                        + " (" + closestFlankingLocation.getState() + ")");

                System.out.println(getName() + " moved to "
                        + _belief.getCurrentPlace().getFormattedCoords());
                intentions.clear();
                return;
            } else {
                intentions.clear();
                System.out.println(getName() + " could not find path for " + closestFlankingLocation.getFormattedCoords()
                        + " (" + closestFlankingLocation.getState() + ")");
                System.out.println(getName() + " could not move to " + closestFlankingLocation.getFormattedCoords()
                        + " (" + closestFlankingLocation.getState() + ")");
            }
        }
        setMode(ROAM_MODE);
        _cooldown = MAX_COOLDOWN_ROUNDS;
        _competitiveCounter = 0;
        roamActions();
    }

    /**
     * In the case of the donkeys there can be a chance that they help their
     * opponents.
     *
     * In that case they will send a message to all nearby opponents (by one
     * block) containing information about clients locations and he then resets
     * to ROAM_MODE and goes towards a random direction.
     */
    public void opponentCooperativeActions() {
        for (Place neighborPlace : getBelief().getVisiblePlaces()) {
            if (!neighborPlace.equals(getBelief().getCurrentPlace())
                    && neighborPlace.hasOpposingAgent(this)) {
                for (Agent agent : neighborPlace.getAgentList()) {
                    sendMessage(agent, AgentMessage.CLIENT_LOCATION_TYPE, getBelief().getClientsLocation());
                }
            }
        }
        setMode(ROAM_MODE);
        roamActions();
    }

    /**
     * Returns the available flanking locations
     *
     * @return A list of flanking locations for the first opponent the agent can
     * see that has available flanking locations or an empty arraylist if not
     * possible
     */
    private List<Place> getFlankingLocations() {
        for (Place visiblePlace : getBelief().getVisiblePlaces()) {
            if (visiblePlace.hasOpposingAgent(this)) {
                return visiblePlace.getTraversablePlaces();
            }
        }
        System.out.println(getName() + " lost the opponent ");
        return new ArrayList<>();
    }

    /**
     * Used to plan the way to solve the agent's current problem depending on
     * the mode he is in.
     *
     * @param shortTermGoal If applicable it is used for calculating the path
     * towards it
     * @return List of intentions to be completed
     */
    public List<Intention> plan(final Place shortTermGoal) {
        List<Intention> intentions = new ArrayList<>();

        switch (_mode) {
            case ROAM_MODE:
                intentions.add(new Intention(Intention.FIND_CLIENT_NAME, shortTermGoal));
                break;
            case KNOWS_CLIENT_LOCATION_MODE:
                intentions.add(new Intention(Intention.CALCULATE_PATH_NAME, shortTermGoal));
                intentions.add(new Intention(Intention.CALCULATE_NEXT_ACTION_NAME, shortTermGoal));
                intentions.add(new Intention(Intention.EXECUTE_NEXT_ACTION_NAME, shortTermGoal));
                break;
            case TRANSFERING_MODE:
                intentions.add(new Intention(Intention.CALCULATE_PATH_NAME, shortTermGoal));
                intentions.add(new Intention(Intention.CALCULATE_NEXT_ACTION_NAME, shortTermGoal));
                intentions.add(new Intention(Intention.EXECUTE_NEXT_ACTION_NAME, shortTermGoal));
                break;
            case COMPETITIVE_MODE:
                intentions.add(new Intention(Intention.CALCULATE_PATH_NAME, shortTermGoal));
                intentions.add(new Intention(Intention.CALCULATE_NEXT_ACTION_NAME, shortTermGoal));
                intentions.add(new Intention(Intention.EXECUTE_NEXT_ACTION_NAME, shortTermGoal));
                break;
            default:
                throw new IllegalArgumentException("Invalid value for mode: "
                        + _mode);
        }
        return intentions;
    }

    private boolean runIntentions(List<Intention> intentions) {
        for (Intention intention : intentions) {
            setIntention(intention);
            if (!intention.run(this)) {
                return false;
            }
            intention.setIsComplete(true);
        }
        return true;
    }

    /**
     * Sets the actions to perform by converting the path found using
     * determineAction to the actions to be performed.
     *
     * If it is appropriate to embark or disembark a client then the appropriate
     * action is set.
     */
    public void setNextAction() {
        if (getBelief().carriesClient()
                && getMode().equals(TRANSFERING_MODE)
                && getBelief().getClientsOnboard().get(0).getDestination().equals(getBelief().getCurrentPlace())) {
            setActions(new Action(Action.DISEMBARK_NAME, this));
            setMode(ROAM_MODE);
        } else if (getMode().equals(KNOWS_CLIENT_LOCATION_MODE)
                && getBelief().getCurrentPlace().getState() != '-'
                && getBelief().getCurrentPlace().getFirstAvailableClient() != null) {
            setActions(new Action(Action.EMBARK_NAME, this));
            setMode(TRANSFERING_MODE);
        } else if (_path.size() != 1) {
            Place current = _path.get(0);
            Place next = _path.get(1);
            _path.remove(0);
            _path.remove(0);
            _actionsToExecute.add(determineAction(current, next));
        }

    }

    /**
     * Adds an Action to the list of actions.
     *
     * @param action The Action to be added
     */
    public void setActions(final Action action) {
        _actionsToExecute.add(action);
    }

    /**
     * Executes the actions in the action list one by one.
     *
     * If the action was successful then the agent's belief will be updated.
     *
     * @return true if execution was complete, false if not.
     */
    public boolean executeActions() {
        boolean success = false;
        for (Action action : _actionsToExecute) {
            System.out.println(getName() + " is executing: " + action.getName());

            final ActionResult ar = action.run(getBelief().getCurrentPlace());
            if (ar.isSucceeded()) {
                System.out.println(getName() + " successfuly executed: " + action.getName());
                _score += ar.getTotalCost();

                if (getBelief().carriesClient()) {
                    getBelief().getCurrentPlace().removeClient(getBelief().getClientsOnboard().get(0));
                }

                getBelief().getCurrentPlace().removeAgent(this);
                getBelief().setCurrentPlace(ar.getNextPlace());
                getBelief().getCurrentPlace().addAgent(this);

                if (getBelief().carriesClient()) {
                    getBelief().getCurrentPlace().addClient(getBelief().getClientsOnboard().get(0));
                }

                success = true;
            }
        }
        _path.clear();
        _actionsToExecute.clear();
        return success;
    }

    /**
     * Determines which action needs to be executed in order to go from the
     * first Place to the second Place depending on the coordinates.
     *
     * @param first Starting Place
     * @param second Place the agent needs to go to
     *
     * @return The Action to be executed
     */
    private Action determineAction(final Place first, final Place second) {
        final int x1 = first.getCoords().getX();
        final int y1 = first.getCoords().getY();
        final int x2 = second.getCoords().getX();
        final int y2 = second.getCoords().getY();

        if (x1 - x2 == 1) {
            return new Action("UP", this);
        } else if (x1 - x2 == -1) {
            return new Action("DOWN", this);
        } else if (y1 - y2 == 1) {
            return new Action("LEFT", this);
        } else if (y1 - y2 == -1) {
            return new Action("RIGHT", this);
        } else {
            throw new IllegalArgumentException("Invalid arguments for determineAction");
        }
    }

    /**
     * Finds the best path from start to end by performing BFS on the world and
     * setting the parents accordingly.
     *
     * The agent will not take in to account Places that are in line-of-sight
     * and have an enemy agent.
     *
     * @param start the starting Place
     * @param end the ending Place
     * @return a list of places which is the most efficient path
     */
    private List<Place> breadthFirstSearch(Place start, final Place end) {
        final Queue<Place> queue = new ArrayDeque<>();
        final List<Place> visited = new ArrayList<>();
        int counter = 0;

        queue.add(start);

        while (!queue.isEmpty()) {
            counter++;
            if (counter > 100) {
                int x = 0;
            }
            start = queue.poll();
            visited.add(start);

            if (start.equals(end)) {
                break;
            }

            for (Place neighbor : start.getTraversablePlaces()) {
                if (neighbor.equals(end) && neighbor.hasOpposingAgent(this)
                        && neighbor.isIn(getBelief().getCurrentPlace().getTraversablePlaces())) {
                    clearParents();
                    return new ArrayList<>();
                }
                if (!neighbor.isIn(visited) && !neighbor.isIn(queue)) {
                    if (!(neighbor.isIn(getBelief().getVisiblePlaces()) && neighbor.hasOpposingAgent(this))) {
                        queue.add(neighbor);
                        neighbor.setParent(start);
                    }
                }
            }
        }
        return getPath(end);
    }

    /**
     * Clears the parents that were set in the Places by BFS.
     */
    private void clearParents() {
        for (Place place : _places) {
            place.setParent(null);
        }
    }

    /**
     * Finds the this Agent object in the list of agents.
     *
     * @param agents the agents list
     * @return the Place object from the list
     */
    public Agent getAgentFromCollection(final List<Agent> agents) {
        for (Agent agent : agents) {
            if (agent.equals(this)) {
                return agent;
            }
        }
        return null;
    }

    /**
     * Checks if an object is equal with this Agent object.
     *
     * @param o the other object
     * @return true if they are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Agent)) {
            return false;
        }

        Agent other = (Agent) o;
        if (!getTeam().equals(other.getTeam())
                || getId() != other.getId()) {
            return false;
        }
        return true;
    }

    /**
     * Hashcode function for use in Maps
     *
     * @return the hashcode of this object
     */
    @Override
    public int hashCode() {
        return (41 * (41 + getId()));
    }

    private final String _team;
    private final int _id;
    private String _mode;

    private Belief _belief;
    private Desire _desire;
    private Intention _intention;

    private final List<AgentMessage> _messages;

    private final List<Agent> _teamates;
    private final List<Place> _places;

    private List<Place> _path;
    private final List<Action> _basicMovementActions;
    private final List<Action> _actionsToExecute;
    private int _score;
    private boolean _roamNextRound;

    /**
     *
     */
    public final static String TRANSFERING_MODE = "TRANSFERING";
    public final static String KNOWS_CLIENT_LOCATION_MODE = "KNOWS_CLIENT_LOCATION";
    public final static String ROAM_MODE = "ROAM";
    public final static String COMPETITIVE_MODE = "COMPETITIVE";
    public final static String OPPONENT_COOPERATIVE_MODE = "OPPONENT_COOPERATIVE";

    private int _competitiveCounter;
    private int _cooldown;

    /**
     * Variable MAX_COMPETITIVE_ROUNDS can be used to alter the agent's maximum
     * rounds he can stay in the competitive mode
     *
     * Variable MAX_COOLDOWN_ROUNDS can be used to alter the agent's maximum
     * rounds he can remain off the competitive mode
     */
    private static final int MAX_COMPETITIVE_ROUNDS = 5;
    private static final int MAX_COOLDOWN_ROUNDS = 5;

    private final Random _rand;
}
