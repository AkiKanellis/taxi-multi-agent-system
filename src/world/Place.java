package world;

import utilities.generalutils.Coordinates;
import agent.Agent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * Place is the class which holds the blocks on our world.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Place {

    /**
     * A Place consists of the coordinates on the world array, it's state
     * (R,G,B,Y if it's a town, - if it isn't), the list of Clients in that
     * Place, the list of agents in that Place, the list of traversable places
     * from this Place, the list of non-traversable places and the parent of the
     * Place (used in BFS).
     *
     * @param coords x,y Coordinates of the Place
     */
    public Place(final Coordinates coords) {
        _coords = coords;
        _state = '-';
        _clients = new ArrayList<>();
        _agents = new ArrayList<>();
        _traversablePlaces = new ArrayList<>();
        _nonTraversablePlaces = new ArrayList<>();
    }

    /**
     *
     * @return x,y coordinates of this place
     */
    public Coordinates getCoords() {
        return _coords;
    }

    /**
     *
     * @return x,y formatted for printing coordinates of this place
     */
    public String getFormattedCoords() {
        return "[" + Integer.toString(getCoords().getX())
                + "," + Integer.toString(getCoords().getY()) + "]";
    }

    /**
     *
     * @param state the state to set (R,G,B,Y,-)
     */
    public void setState(final char state) {
        if (state != '-'
                && state != 'R'
                && state != 'G'
                && state != 'B'
                && state != 'Y') {
            throw new IllegalArgumentException("Invalid value for state: " + state);
        }
        _state = state;
    }

    /**
     *
     * @return the state of this place
     */
    public char getState() {
        return _state;
    }

    /**
     *
     * @param client the client to add
     */
    public void addClient(Client client) {
        _clients.add(client);
    }

    /**
     * Removes the unique client from the list (deletes him from the world).
     *
     * @param client the client to remove
     */
    public void removeClient(final Client client) {
        Iterator<Client> iter = _clients.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(client)) {
                iter.remove();
            }
        }
    }

    /**
     *
     * @return client list of this place
     */
    public List<Client> getClientList() {
        return _clients;
    }

    /**
     *
     * @return the first client that is not taken or null
     */
    public Client getFirstAvailableClient() {
        for (Client client : _clients) {
            if (!client.isTaken()) {
                return client;
            }
        }
        return null;
    }

    /**
     *
     * @param agent the agent to add
     */
    public void addAgent(Agent agent) {
        _agents.add(agent);
    }

    /**
     * Removes the unique agent from the list.
     *
     * @param agent the agent to remove
     */
    public void removeAgent(Agent agent) {
        Iterator<Agent> iter = _agents.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(agent)) {
                iter.remove();
            }
        }
    }

    /**
     *
     * @return agent list of this place
     */
    public List<Agent> getAgentList() {
        return _agents;
    }

    /**
     *
     * @return true if there is any agent in this place, false if not
     */
    public boolean hasAgent() {
        return !_agents.isEmpty();
    }

    /**
     * Checks to see if the place has an opposing agent from the one we provide
     *
     * @param thisAgent the agent we provide
     * @return true if it has an opposing agent, false if not
     */
    public boolean hasOpposingAgent(Agent thisAgent) {
        for (Agent agent : _agents) {
            if (!thisAgent.getTeam().equals(agent.getTeam())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the place has a teamate agent (other than the one we
     * provide) of the one we provide.
     *
     * @param thisAgent the agent we provide
     * @return true if it has a teamate agent, false if not
     */
    public boolean hasTeamateAgent(Agent thisAgent) {
        for (Agent agent : _agents) {
            if (thisAgent.getTeam().equals(agent.getTeam())
                    && !thisAgent.equals(agent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a non-traversable Place to the list.
     *
     * @param place The Place to be added
     */
    public void addNonTraversablePlace(final Place place) {
        _nonTraversablePlaces.add(place);
    }

    /**
     *
     * @return non traversable places list of this place
     */
    public List<Place> getNonTraversablePlaces() {
        return _nonTraversablePlaces;
    }

    /**
     * Adds a traversable Place to the list.
     *
     * @param place The Place to be added
     */
    public void addTraversablePlace(final Place place) {
        _traversablePlaces.add(place);
    }

    /**
     *
     * @return traversable places list of this place
     */
    public List<Place> getTraversablePlaces() {
        return _traversablePlaces;
    }

    /**
     * Sets the parent (the Place where the search came from in BFS).
     *
     * @param parent the place to set as parent
     */
    public void setParent(final Place parent) {
        _parent = parent;
    }

    /**
     *
     * @return parent of this place (for BFS)
     */
    public Place getParent() {
        return _parent;
    }

    /**
     * Checks if the Place is in the given list of places
     *
     * @param places The given list of places
     * @return True if it is, false if not
     */
    public boolean isIn(final List<Place> places) {
        return places.stream().anyMatch((pl) -> (pl.equals(this)));
    }

    /**
     * Checks if the Place is in the given queue of places
     *
     * @param places The given queue of places
     * @return true if it is, false if not
     */
    public boolean isIn(final Queue<Place> places) {
        return places.stream().anyMatch((pl) -> (pl.equals(this)));
    }

    /**
     * Finds the Place object in the list given and returns it.
     *
     * @param places The given list of places
     * @return if found, the Place object from the list otherwise null
     */
    public Place getPlaceFromCollection(final List<Place> places) {
        for (Place place : places) {
            if (place.equals(this)) {
                return place;
            }
        }
        return null;
    }

    /**
     * Checks if an object is equal with this Place object.
     *
     * @param o the other object
     * @return true if they are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Place)) {
            return false;
        }

        Place other = (Place) o;
        if (!this.getCoords().equals(other.getCoords())) {
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
        return (41 * (41 + getCoords().getX()) - getCoords().getY());
    }

    private final Coordinates _coords;
    private char _state;
    private final List<Client> _clients;
    private final List<Agent> _agents;
    private final List<Place> _traversablePlaces;
    private final List<Place> _nonTraversablePlaces;
    private Place _parent;
}
