package world;

import utilities.generalutils.Coordinates;
import agent.Agent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The World object is the main class that represents the world of the problem.
 *
 * It holds all the places and several functions for producing the world.
 */
public class World {

    /**
     * The World object holds the world array which represents the 2D position
     * of the places, the list of towns in the world and the list of places.
     *
     * @param width World's width
     * @param height World's height
     * @param rand The random object
     */
    public World(final int width, final int height, final Random rand) {
        if (width != 5) {
            throw new IllegalArgumentException("Invalid value for width: " + width);
        }
        _width = width;

        if (height != 5) {
            throw new IllegalArgumentException("Invalid value for width: " + height);
        }
        _height = height;

        _world = new Place[_width][_height];
        _places = new ArrayList<>();
        for (int i = 0; i < _height; i++) {
            for (int j = 0; j < _width; j++) {
                _world[i][j] = new Place(new Coordinates(i, j));
                _places.add(_world[i][j]);
            }
        }
        setStates();
        _towns = new ArrayList<>();
        setTowns();
        setNonTraversablePlaces();

        _rand = rand;
        _shuffledIndex = new ArrayList<>();
        for (int i = 0; i < _towns.size(); i++) {
            _shuffledIndex.add(i);
        }
        defineNeighbors();
    }

    /**
     *
     * @return the places list
     */
    public List<Place> getPlaces() {
        return _places;
    }

    /**
     *
     * @return the number of clients in the world
     */
    private int getNumberOfClients() {
        int numOfClients = 0;
        for (Place place : _places) {
            numOfClients += place.getClientList().size();
        }
        return numOfClients;
    }

    /**
     * Sets a random Place as the agent's starting location.
     *
     * @param agent The agent to set.
     * @return The Place in which the Agent was put at.
     */
    public Place setRandomAgent(Agent agent) {
        int rand;
        do {
            rand = _rand.nextInt(25 - 0) + 0;
        } while (!_places.get(rand).getAgentList().isEmpty());

        _places.get(rand).addAgent(agent);
        return _places.get(rand);
    }

    /**
     * This is to run periodically in the run of the world.
     *
     * Each time it runs it checks how many clients could be put in the world,
     * it then creates a unique random id for the client and puts him randomly
     * in a town where it has no agents (so that it won't be unfair to the other
     * agents).
     */
    public void setRandomClients() {
        final int remainingClients = MAX_CLIENTS - getNumberOfClients();
        for (int i = 0; i < remainingClients; i++) {
            int randID;
            do {
                randID = _rand.nextInt(MAX_CLIENTS - 0) + 0;
            } while (idExists(randID));

            Collections.shuffle(_shuffledIndex, _rand);

            for (int j : _shuffledIndex) {
                if (!_towns.get(j).hasAgent()) {
                    _towns.get(j).addClient(new Client(randID, _towns.get(_rand.nextInt(_towns.size() - 1 - 0) + 0)));
                    break;
                }
            }
        }
    }

    /**
     * @return true if the id already exists in the world, false if not
     */
    private boolean idExists(final int id) {
        for (Place place : _places) {
            for (Client client : place.getClientList()) {
                if (id == client.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the states of the four towns.
     */
    private void setStates() {
        _world[0][0].setState('R');
        _world[0][4].setState('G');
        _world[4][0].setState('Y');
        _world[4][3].setState('B');
    }

    /**
     * Adds the towns that were set to the _towns list.
     */
    private void setTowns() {
        _towns.add(_world[0][0]);
        _towns.add(_world[0][4]);
        _towns.add(_world[4][0]);
        _towns.add(_world[4][3]);
    }

    /**
     * Sets the pairs of places where traversing is not feasible and/or they are
     * not visible to each other.
     */
    private void setNonTraversablePlaces() {
        _world[3][0].addNonTraversablePlace(_world[3][1]);
        _world[3][0].addNonTraversablePlace(_world[4][1]);

        _world[4][0].addNonTraversablePlace(_world[4][1]);
        _world[4][0].addNonTraversablePlace(_world[3][1]);

        _world[0][1].addNonTraversablePlace(_world[0][2]);
        _world[0][1].addNonTraversablePlace(_world[1][2]);

        _world[1][1].addNonTraversablePlace(_world[1][2]);
        _world[1][1].addNonTraversablePlace(_world[0][2]);

        _world[3][2].addNonTraversablePlace(_world[3][3]);
        _world[3][2].addNonTraversablePlace(_world[4][3]);

        _world[4][2].addNonTraversablePlace(_world[4][3]);
        _world[4][2].addNonTraversablePlace(_world[3][3]);

        _world[3][1].addNonTraversablePlace(_world[3][0]);
        _world[3][1].addNonTraversablePlace(_world[4][0]);

        _world[4][1].addNonTraversablePlace(_world[4][0]);
        _world[4][1].addNonTraversablePlace(_world[3][0]);

        _world[0][2].addNonTraversablePlace(_world[0][1]);
        _world[0][2].addNonTraversablePlace(_world[1][1]);

        _world[1][2].addNonTraversablePlace(_world[0][1]);
        _world[1][2].addNonTraversablePlace(_world[1][1]);

        _world[3][3].addNonTraversablePlace(_world[3][2]);
        _world[3][3].addNonTraversablePlace(_world[4][2]);

        _world[4][3].addNonTraversablePlace(_world[4][2]);
        _world[4][3].addNonTraversablePlace(_world[3][2]);
    }

    /**
     * Defines the neighbours of each Place.
     */
    private void defineNeighbors() {
        _places.stream().forEach((place) -> {
            _places.stream().filter((neighbor) -> (!neighbor.isIn(place.getNonTraversablePlaces())))
                    .forEach((neighbor) -> {
                        final int x1 = place.getCoords().getX();
                        final int y1 = place.getCoords().getY();
                        final int x2 = neighbor.getCoords().getX();
                        final int y2 = neighbor.getCoords().getY();
                        if ((Math.abs(x2 - x1) == 1 && Math.abs(y2 - y1) == 0)
                        || (Math.abs(x2 - x1) == 0 && Math.abs(y2 - y1) == 1)) {
                            place.addTraversablePlace(neighbor);
                        }
                    });
        });
    }

    List<Integer> _shuffledIndex;
    final Random _rand;

    final int _width;
    final int _height;
    Place[][] _world;
    List<Place> _towns;
    List<Place> _places;
    
    /**
     * The maximum number of clients to be in the world at any given time.
     */
    private final static int MAX_CLIENTS = 10;
}
