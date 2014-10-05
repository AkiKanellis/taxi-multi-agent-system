package utilities.generalutils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import world.Place;

/**
 * This is a static class used for various general utility methods such as print
 * methods, sorting methods and mathematic methods.
 *
 * @author Kanakari Maria
 * @author Kanellis Dimitris
 */
public class Helper {

    /**
     * Print method for the case of an agent mode change.
     *
     * @param agentName the agent's name
     * @param previousMode the mode he was in
     * @param currentMode the mode he is going to be set at
     */
    public static void modeChangePrint(final String agentName, final String previousMode, final String currentMode) {
        System.out.println(agentName
                + " changed from " + previousMode + " to " + currentMode);
    }

    /**
     * Print method for the case of an agent replying to a message.
     *
     * @param senderName the sender's name
     * @param recipientName the recipient's name
     * @param messageType the type of message
     */
    public static void messageReplyPrint(final String senderName, final String recipientName, final String messageType) {
        System.out.print(senderName
                + " is replying to " + recipientName + " :"
                + "\n\tHeader: " + "[" + messageType + "]"
                + "\n\tContent: EMPTY");
        System.out.println();
    }

    /**
     * Print method for the case of an agent sending a message.
     *
     * @param senderName the sender's name
     * @param recipientName the recipient's name
     * @param messageType the type of message
     * @param content the content of the message
     */
    public static void messageSendPrint(final String senderName, final String recipientName, final String messageType, final List<Place> content) {
        System.out.print(senderName
                + " is sending a message to " + recipientName + " :"
                + "\n\tHeader: " + "[" + messageType + "]"
                + "\n\tContent: ");
        for (Place place : content) {
            System.out.print(place.getFormattedCoords() + " ");
        }
        System.out.println();
    }

    /**
     * Works just like #messageSendPrint(java.lang.String, java.lang.String,
     * java.lang.String, java.util.List) except this one does not have any
     * content.
     *
     * @see #messageSendPrint(java.lang.String, java.lang.String,
     * java.lang.String, java.util.List)
     */
    public static void messageSendPrint(final String senderName, final String recipientName, final String messageType) {
        System.out.print(senderName
                + " is sending a message to " + recipientName + " :"
                + "\n\tHeader: " + "[" + messageType + "]"
                + "\n\tContent: ");
        System.out.println();
    }

    /**
     * Print method for the case of a path found by an agent
     *
     * @param agentName the agent's name
     * @param placeFormattedCoords the place's formatted coordinates
     * @param placeState the place's state
     */
    public static void foundPathPrint(final String agentName, final String placeFormattedCoords, final char placeState) {
        System.out.println(agentName + " found path for " + placeFormattedCoords
                + " (" + placeState + ")");
    }

    /**
     * Print method for the case of a path not found by an agent
     *
     * @param agentName the agent's name
     * @param placeFormattedCoords the place's formatted coordinates
     * @param placeState the place's state
     */
    public static void didNotFindPathPrint(final String agentName, final String placeFormattedCoords, final char placeState) {
        System.out.println(agentName + " could not find path for " + placeFormattedCoords
                + " (" + placeState + ")");
    }

    /**
     * Print method for the case of an agent successfully moving to a place
     *
     * @param agentName the agent's name
     * @param placeFormattedCoords the place's formatted coordinates
     * @param placeState the place's state
     */
    public static void movedToPrint(final String agentName, final String placeFormattedCoords, final char placeState) {
        System.out.println(agentName + " moved to " + placeFormattedCoords
                + " (" + placeState + ")");
    }

    /**
     * Print method for the case of an agent not managing to move to a place.
     *
     * @param agentName the agent's name
     * @param placeFormattedCoords the place's formatted coordinates
     * @param placeState the place's state
     */
    public static void didNotMoveToPrint(final String agentName, final String placeFormattedCoords, final char placeState) {
        System.out.println(agentName + " could not move to " + placeFormattedCoords
                + " (" + placeState + ")");
    }

    /**
     * Method for checking if a number is between two other number (inclusive
     * both ways).
     *
     * @param x the number to check
     * @param lower the lower limit
     * @param upper the upper limit
     * @return true if it is between, false if not
     */
    public static boolean isBetween(final int x, final int lower, final int upper) {
        return lower <= x && x <= upper;
    }

    /**
     * Method for finding the distance between two places with their x and y
     * coordinates.
     *
     * @param first the first place
     * @param second the second place
     * @return the distance between these two places
     */
    public static double getDistance(Place first, Place second) {
        final double x1 = first.getCoords().getX();
        final double y1 = first.getCoords().getY();

        final double x2 = second.getCoords().getX();
        final double y2 = second.getCoords().getY();

        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Method for sorting a HashMap by values and not by keys.
     *
     * @param map the map to sort
     * @return the new sorted map
     */
    public static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here we are copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
