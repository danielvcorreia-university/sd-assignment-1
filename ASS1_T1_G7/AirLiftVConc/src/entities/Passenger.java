package entities;

import sharedRegions.DepartureAirport;
import sharedRegions.Plane;

/**
 *   Passenger thread.
 *
 *   It simulates the passenger life cycle.
 *   Static solution.
 */

public class Passenger extends Thread
{
    /**
     *  Passenger identification.
     */

    private int passengerId;

    /**
     *  Passenger state.
     */

    private int passengerState;

    /**
     *  True if the passenger has been called by the hostess to show his documents.
     */

    private boolean readyToShowDocuments;

    /**
     *  Reference to the departure airport.
     */

    private final DepartureAirport depAirport;

    /**
     *  Reference to the plane.
     */

    private final Plane plane;

    /**
     *   Instantiation of a passenger thread.
     *
     *     @param name thread name
     *     @param passengerId passenger id
     *     @param depAirport reference to the departure airport
     *     @param plane reference to the plane
     */

    public Passenger (String name, int passengerId, DepartureAirport depAirport, Plane plane)
    {
        super (name);
        this.passengerId = passengerId;
        passengerState = PassengerStates.GOING_TO_AIRPORT;
        this.depAirport = depAirport;
        this.plane = plane;
        this.readyToShowDocuments = false;
    }

    /**
     *   Set passenger id.
     *
     *     @param id passenger id
     */

    public void setPassengerId (int id)
    {
        passengerId = id;
    }

    /**
     *   Get passenger id.
     *
     *     @return passenger id
     */

    public int getPassengerId ()
    {
        return passengerId;
    }

    /**
     *   Set if passenger is ready to show documents to hostess.
     *
     *     @param bool ready to show documents
     */

    public void setReadyToShowDocuments (boolean bool)
    {
        readyToShowDocuments = bool;
    }

    /**
     *   Get ready to show documents.
     *
     *     @return ready to show documents
     */

    public boolean getReadyToShowDocuments ()
    {
        return readyToShowDocuments;
    }

    /**
     *   Set passenger state.
     *
     *     @param state new passenger state
     */

    public void setPassengerState (int state)
    {
        passengerState = state;
    }

    /**
     *   Get passenger state.
     *
     *     @return passenger state
     */

    public int getPassengerState ()
    {
        return passengerState;
    }

    /**
     *   Life cycle of the passenger.
     */

    @Override
    public void run ()
    {
        this.travelToAirport()	;	            // Takes random time
        DepartureAirport.waitInQueue();
        DepartureAirport.showDocuments();
        plane.boardThePlane();
        plane.waitForEndOfFlight();
        plane.leaveThePlane();             //see you later aligator
    }

    /**
     *  Travel to airport.
     *
     *  Internal operation.
     */

    private void travelToAirport ()
    {
        try
        { sleep ((long) (1 + 100 * Math.random ()));
        }
        catch (InterruptedException e) {}
    }
}

