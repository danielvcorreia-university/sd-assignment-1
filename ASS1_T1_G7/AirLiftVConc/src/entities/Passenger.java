package entities;

import genclass.GenericIO;
import main.SimulPar;
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
     * @param destAirport reference to the destination airport
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
     * @return True if ready to show documents
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
        boolean reportFinalInfo = false;

        this.travelToAirport();                // Takes random time
        depAirport.waitInQueue();
        depAirport.showDocuments();
        depAirport.boardThePlane();
        plane.setInF(Plane.getInF()+1);
        plane.waitForEndOfFlight();
        plane.leaveThePlane();             //see you later aligator
        destAirport.incPTAL();
    }

    /**
     * Travel to airport.
     * <p>
     * Internal operation. Sleeps for an amount of time.
     */

    private void travelToAirport() {
        try {
            sleep((long) (1 + 400 * Math.random()));
        } catch (InterruptedException e) {
            GenericIO.writelnString("Interruption: " + e.getMessage());
            System.exit(1);
        }
    }
}

