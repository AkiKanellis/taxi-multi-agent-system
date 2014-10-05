package utilities.generalutils;

/**
 * This class is used to hold the coordinates of the places.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Coordinates {

    /**
     * @param x Coordinate x
     * @param y Coordinate y
     */
    public Coordinates(final int x, final int y) {
        if (!(x >= 0 && x <= 4)) {
            throw new IllegalArgumentException("Invalid value for x: " + x);
        }
        _x = x;

        if (!(y >= 0 && y <= 4)) {
            throw new IllegalArgumentException("Invalid value for y: " + y);
        }
        _y = y;
    }

    /**
     * Sets the coordinate as long as it is not out of bounds.
     *
     * @param x the X coordinate to set
     */
    public void setX(final int x) {
        if (!(x >= 0 && x <= 4)) {
            throw new IllegalArgumentException("Invalid value for x: " + x);
        }
        _x = x;
    }

    /**
     * @return the X coordinate
     */
    public int getX() {
        return _x;
    }

    /**
     * Sets the coordinate as long as it is not out of bounds.
     *
     * @param y the Y coordinate to set
     */
    public void setY(final int y) {
        if (!(y >= 0 && y <= 4)) {
            throw new IllegalArgumentException("Invalid value for y: " + y);
        }
        _y = y;
    }

    /**
     * @return the Y coordinate
     */
    public int getY() {
        return _y;
    }

    /**
     * Checks if an object is equal with this Coordinates object.
     *
     * @param o the other object
     * @return true if they are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Coordinates)) {
            return false;
        }

        Coordinates other = (Coordinates) o;
        if (this.getX() != other.getX() || this.getY() != other.getY()) {
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
        return (41 * (41 + this.getX()) - this.getY());
    }

    private int _x;
    private int _y;
}
