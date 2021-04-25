package entities;

import genclass.GenericIO;
import sharedRegions.DepartureAirport;
import sharedRegions.DestinationAirport;
import sharedRegions.Plane;

/**
 *   Passenger thread.
 *
 *   It simulates the passenger life cycle.
 *   Static solution.
 */

public class Passenger extends Thread {
    /**
     * Passenger identification.
     */

    private int passengerId;

    /**
     * Passenger state.
     */

    private int passengerState;

    /**
     * True if the passenger has been called by the hostess to show his documents.
     */

    private boolean readyToShowDocuments;

    /**
     * Reference to the departure airport.
     */

    private final DepartureAirport depAirport;

    /**
     * Reference to the plane.
     */

    private final Plane plane;

    /**
     * Reference to the destination airport.
     */

    private final DestinationAirport destAirport;

    /**
     * Instantiation of a passenger thread.
     *
     * @param name        thread name
     * @param passengerId passenger id
     * @param depAirport  reference to the departure airport
     * @param plane       reference to the plane
     */

    public Passenger(String name, int passengerId, DepartureAirport depAirport, Plane plane, DestinationAirport destAirport) {
        super(name);
        this.readyToShowDocuments = false;
        this.passengerId = passengerId;
        passengerState = PassengerStates.GOING_TO_AIRPORT;
        this.depAirport = depAirport;
        this.plane = plane;
        this.readyToShowDocuments = false;
        this.destAirport = destAirport;
    }

    /**
     * Set passenger id.
     *
     * @param id passenger id
     */

    public void setPassengerId(int id) {
        passengerId = id;
    }

    /**
     * Get passenger id.
     *
     * @return passenger id
     */

    public int getPassengerId() {
        return passengerId;
    }

    /**
     * Set if passenger is ready to show documents to hostess.
     *
     * @param bool ready to show documents
     */

    public void setReadyToShowDocuments(boolean bool) {
        readyToShowDocuments = bool;
    }

    /**
     * Get ready to show documents.
     *
     * @return ready to show documents
     */

    public boolean getReadyToShowDocuments() {
        return readyToShowDocuments;
    }

    /**
     * Set passenger state.
     *
     * @param state new passenger state
     */

    public void setPassengerState(int state) {
        passengerState = state;
    }

    /**
     * Get passenger state.
     *
     * @return passenger state
     */

    public int getPassengerState() {
        return passengerState;
    }

    /**
     * Life cycle of the passenger.
     */

    @Override
    public void run() {
        System.out.println("passenger travel to airport");
        this.travelToAirport();                // Takes random time
        System.out.println("passenger wait in queue");
        depAirport.waitInQueue();
        System.out.println("passenger show documents");
        depAirport.showDocuments();
        System.out.println("passenger board the plane");
        depAirport.boardThePlane();
        System.out.println("passenger wait for end of flight");
        plane.waitForEndOfFlight();
        plane.leaveThePlane();             //see you later aligator
        destAirport.incPTAL();
    }

    /**
     * Travel to airport.
     * <p>
     * Internal operation.
     */

    private void travelToAirport() {
        try {
            sleep((long) (1 + 200 * Math.random()));
        } catch (InterruptedException e) {
            GenericIO.writelnString("Interruption: " + e.getMessage());
            System.exit(1);
        }
    }
}

