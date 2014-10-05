package agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import world.Client;
import world.Place;

/**
 * This class holds the Belief of the agent.
 *
 * The belief consists of the agent's current Place, the places that are in his
 * line-of-sight, the opponents locations that are in line-of-sight, his
 * teamates locations, known clients locations (which may or may not be true),
 * the clients that are onboard and if his desire is complete.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Belief {

    /**
     * @param currentPlace The current position of the agent
     */
    public Belief(Place currentPlace) {
        if (currentPlace == null) {
            throw new IllegalArgumentException("Invalid arguments for belief");
        }
        _currentPlace = currentPlace;
        _visiblePlaces = new ArrayList<>();
        _opponentsLocations = new ArrayList<>();
        _teamatesLocation = new ArrayList<>();
        _clientsLocations = new ArrayList<>();
        _clientsOnboard = new ArrayList<>();
        _desireComplete = false;
    }

    /**
     *
     * @param currentPlace the current place of the agent
     */
    public void setCurrentPlace(final Place currentPlace) {
        _currentPlace = currentPlace;
    }

    /**
     *
     * @return the current place of the agent
     */
    public Place getCurrentPlace() {
        return _currentPlace;
    }

    /**
     * Sets the visible places of the agent to all the places that are one block
     * away unless there is a wall in between.
     *
     * @param places the places that are in line-of-sight of the agent
     */
    public void setVisiblePlaces(final List<Place> places) {
        _visiblePlaces.clear();

        final int agentX = _currentPlace.getCoords().getX();
        final int agentY = _currentPlace.getCoords().getY();

        int x;
        int y;

        for (Place place : places) {
            x = place.getCoords().getX();
            y = place.getCoords().getY();
            if (Math.abs(agentX - x) <= 1
                    && Math.abs(agentY - y) <= 1
                    && !place.isIn(_currentPlace.getNonTraversablePlaces())) {
                _visiblePlaces.add(place);
            }
        }
    }

    /**
     * Checks the visible places to see if there is an opponent in any of them.
     *
     * @return true if opponent is in line-of-sight, false if not
     */
    public boolean opponentNearby() {
        for (Place place : _visiblePlaces) {
            for (Place opponentLocation : _opponentsLocations) {
                if (place.equals(opponentLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks the visible places to see if there is a client in any of them.
     *
     * @return true if client is in line-of-sight, false if not
     */
    public boolean clientNearby() {
        for (Place place : _visiblePlaces) {
            for (Place clientLocation : _clientsLocations) {
                if (place.equals(clientLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @return the visible places of the agent
     */
    public List<Place> getVisiblePlaces() {
        return _visiblePlaces;
    }

    /**
     * Adds a client to the onboard client list of the agent as long as he is
     * not taken.
     *
     * @param client the client to add
     */
    public void addClientOnboard(final Client client) {
        client.setIsTaken(true);
        _clientsOnboard.add(client);
    }

    /**
     * Removes a client from the onboard client list of the agent.
     *
     * @param client the client to remove
     */
    public void removeClientOnboard(final Client client) {
        Iterator<Client> iter = _clientsOnboard.iterator();
        while (iter.hasNext()) {
            Client clientOnboard = iter.next();
            if (client.equals(clientOnboard)) {
                iter.remove();
            }
        }
    }

    /**
     *
     * @return the clients that the agent is currently transferring
     */
    public List<Client> getClientsOnboard() {
        return _clientsOnboard;
    }

    /**
     *
     * @return true if the agent has a client onboard, false if not
     */
    public boolean carriesClient() {
        return !_clientsOnboard.isEmpty();
    }

    /**
     *
     * @return the visible opponents from the position of the agent
     */
    public List<Place> getOpponentsLocation() {
        return _opponentsLocations;
    }

    /**
     * Adds an opponent to the opponents location list of the agent.
     *
     * @param opponentLocation the opponent to add
     */
    public void addOpponentLocation(Place opponentLocation) {
        _opponentsLocations.add(opponentLocation);
    }

    /**
     *
     * @return the teamates location list
     */
    public List<Place> getTeamatesLocation() {
        return _teamatesLocation;
    }

    /**
     * Adds a teamate to the teamates location list of the agent.
     *
     * @param teamatesLocation the teamate to add
     */
    public void addTeamatesLocation(Place teamatesLocation) {
        _teamatesLocation.add(teamatesLocation);
    }

    /**
     *
     * @return true if the agent believes he knows at least one client's
     * location, false if not
     */
    public boolean knowsClientLocation() {
        return !_clientsLocations.isEmpty();
    }

    /**
     *
     * @return the client location list
     */
    public List<Place> getClientsLocation() {
        return _clientsLocations;
    }

    /**
     * Adds the new clients location to the current clients location as long as
     * the client does not already exist in the client location list.
     *
     * @param newClientsLocations the new clients locations list to add
     */
    public void addClientsLocation(List<Place> newClientsLocations) {
        for (Place newClientLocation : newClientsLocations) {
            if (!newClientLocation.isIn(_clientsLocations)) {
                _clientsLocations.add(newClientLocation);
            }
        }
    }

    /**
     * Works exactly like #addClientsLocation(java.util.List) except here we can
     * add a single client.
     *
     * @see #addClientsLocation(java.util.List)
     *
     * @param newClientLocation the client to add
     */
    public void addClientsLocation(Place newClientLocation) {
        if (!newClientLocation.isIn(_clientsLocations)) {
            _clientsLocations.add(newClientLocation);
        }
    }

    /**
     * In this method the agent cross references his client location list with
     * the visible places.
     *
     * If he finds that one of his visible places matches a clients location he
     * believed was true and no client is there anymore he then procedes to
     * remove that client location from his list since it is no longer valid.
     *
     * For example we have a client that is at [0,4] and when the agent is in
     * line-of-sight of that place he sees that no client is there, then he will
     * remove that client location ([0,4] from the _clientsLocations list.
     */
    public void removeRedundantClients() {
        Iterator<Place> iter = _clientsLocations.iterator();
        while (iter.hasNext()) {
            Place clientLocation = iter.next();
            if (clientLocation.isIn(_visiblePlaces) && clientLocation.getFirstAvailableClient() == null) {
                iter.remove();
            }
        }
    }

    /**
     *
     * @param desireComplete sets the desire of the agent
     */
    public void setDesireComplete(final boolean desireComplete) {
        _desireComplete = desireComplete;
    }

    /**
     *
     * @return true if the desire is complete, false if not
     */
    public boolean isDesireComplete() {
        return _desireComplete;
    }

    private Place _currentPlace;
    private final List<Place> _visiblePlaces;
    private List<Place> _opponentsLocations;
    private List<Place> _teamatesLocation;
    private List<Place> _clientsLocations;
    private List<Client> _clientsOnboard;
    private boolean _desireComplete;
}
