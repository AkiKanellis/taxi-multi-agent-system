package world;

/**
 * The Client class is used for creating the clients in the world. Each Client
 * is unique.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Client {

    /**
     * Each Client has a unique id, a random destination (a town) and if he is
     * taken already.
     *
     * @param id The unique id of the client.
     * @param destination The random destination of the client.
     */
    public Client(final int id, final Place destination) {
        _id = id;
        _destination = destination;
        _isTaken = false;
    }

    /**
     *
     * @return the id of the client
     */
    public int getId() {
        return _id;
    }

    /**
     *
     * @return the destination of the client
     */
    public Place getDestination() {
        return _destination;
    }

    /**
     *
     * @return true if the client is taken, false if not
     */
    public boolean isTaken() {
        return _isTaken;
    }

    /**
     *
     * @param isTaken if the client is taken or not
     */
    public void setIsTaken(final boolean isTaken) {
        _isTaken = isTaken;
    }

    /**
     * Checks if an object is equal with this Client object.
     *
     * @param o the other object
     * @return true if they are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Client)) {
            return false;
        }

        Client other = (Client) o;
        if (this.getId() != other.getId()) {
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
        return (41 * (41 + this.getId()));
    }

    private final int _id;
    private final Place _destination;
    private boolean _isTaken;
}
