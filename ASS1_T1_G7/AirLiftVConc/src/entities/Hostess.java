package entities;

import main.SimulPar;
import sharedRegions.DepartureAirport;
import sharedRegions.DestinationAirport;
import sharedRegions.Plane;

/**
 *   Hostess thread.
 *
 *   It simulates the hostess life cycle.
 *   Static solution.
 */

public class Hostess extends Thread {

    /**
     * Hostess identification.
     */

    private int hostessId;

    /**
     * Hostess state.
     */

    private int hostessState;

    /**
     * Count number of passengers on the plane.
     */

    private int hostessCount;

    /**
     * Count number of passengers checked by hostess.
     */

    private int checkedPassengers;

    /**
     * True if there is any passenger in queue for the hostess to process.
     */

    private boolean passengerInQueue;

    /**
     * True if the hostess can check next passenger documents.
     */

    private boolean readyForNextPassenger;

    /**
     * True if the passenger has given his documents to the hostess for her to check.
     */

    private boolean readyToCheckDocuments;

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
     * Instantiation of a hostess thread.
     *
     * @param name        thread name
     * @param hostessId   hostess id
     * @param depAirport  reference to the departure airport
     * @param plane       reference to the plane
     * @param destAirport reference to the destination airport
     */

    public Hostess(String name, int hostessId, DepartureAirport depAirport, Plane plane, DestinationAirport destAirport) {
        super(name);
        this.readyToCheckDocuments = false;
        this.readyForNextPassenger = false;
        this.hostessCount = 0;
        this.checkedPassengers = 0;
        this.hostessId = hostessId;
        hostessState = HostessStates.WAIT_FOR_FLIGHT;
        this.depAirport = depAirport;
        this.plane = plane;
        this.destAirport = destAirport;
    }

    /**
     * Set hostess id.
     *
     * @param id hostess id
     */

    public void setHostessId(int id) {
        hostessId = id;
    }

    /**
     * Get hostess id.
     *
     * @return hostess id
     */

    public int getHostessId() {
        return hostessId;
    }

    /**
     * Set hostess count.
     *
     * @param count hostess count
     */

    public void setHostessCount(int count) {
        hostessCount = count;
    }

    /**
     * Get hostess count.
     *
     * @return hostess count
     */

    public int getHostessCount() {
        return hostessCount;
    }

    /**
     * Set number of passengers which hostess checked documents.
     *
     * @param nCheckedPassengers number of passengers checked
     */

    public void setCheckedPassengers(int nCheckedPassengers) { checkedPassengers = nCheckedPassengers; }

    /**
     * Get number of passengers which hostess checked documents.
     *
     * @return checked passengers
     */

    public int getCheckedPassengers() {
        return checkedPassengers;
    }

    /**
     * Set if there is any passenger in queue for the hostess to process
     *
     * @param bool passenger in queue
     */

    public void setPassengerInQueue(boolean bool) {
        passengerInQueue = bool;
    }

    /**
     * Check if there is any passenger in queue for the hostess to process
     *
     * @return True if passenger in queue
     */

    public boolean getPassengerInQueue() {
        return passengerInQueue;
    }

    /**
     * Set if hostess is ready to check documents of the next passenger
     *
     * @param bool ready for next passenger
     */

    public void setReadyForNextPassenger(boolean bool) {
        readyForNextPassenger = bool;
    }

    /**
     * Get ready to check documents of the next passenger
     *
     * @return True if ready for next passenger
     */

    public boolean getReadyForNextPassenger() {
        return readyForNextPassenger;
    }

    /**
     * Set if hostess has received the documents from the passenger.
     *
     * @param bool ready to check documents
     */

    public void setReadyToCheckDocuments(boolean bool) {
        readyToCheckDocuments = bool;
    }

    /**
     * Get ready to check documents.
     *
     * @return ready to check documents
     */

    public boolean getReadyToCheckDocuments() {
        return readyToCheckDocuments;
    }

    /**
     * Set hostess state.
     *
     * @param state new hostess state
     */

    public void setHostessState(int state) {
        hostessState = state;
    }

    /**
     * Get hostess state.
     *
     * @return hostess state
     */

    public int getHostessState() {
        return hostessState;
    }

    /**
     * Life cycle of the hostess.
     */

    @Override
    public void run() {
        boolean endOp = false;                                       // flag signaling end of operations
        plane.waitForNextFlight(true);
        while (!endOp) {
            depAirport.prepareForPassBoarding();

            while (getHostessCount() < SimulPar.MIN) {
                depAirport.checkDocuments();
                depAirport.waitForNextPassenger();
                if (getHostessCount() + getCheckedPassengers() == SimulPar.N) {
                    endOp = true; break;
                }
            }
            while (getPassengerInQueue() && getHostessCount() < SimulPar.MAX) {
                depAirport.checkDocuments();
                depAirport.waitForNextPassenger();
                if (getHostessCount() + getCheckedPassengers() == SimulPar.N) {
                    endOp = true; break;
                }
            }
            plane.informPlaneReadyToTakeOff();

            plane.waitForNextFlight(false);
        }
    }
}