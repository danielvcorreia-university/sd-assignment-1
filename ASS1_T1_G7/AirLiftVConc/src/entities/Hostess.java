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
     * @param name       thread name
     * @param hostessId  hostess id
     * @param depAirport reference to the departure airport
     * @param plane      reference to the plane
     */

    public Hostess(String name, int hostessId, DepartureAirport depAirport, Plane plane, DestinationAirport destAirport) {
        super(name);
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
        System.out.println("hostess start");
        System.out.println("hostess wait for next flight");
        plane.waitForNextFlight();
        while (!endOp) {
            System.out.println("hostess prepare for boarding");
            depAirport.prepareForPassBoarding();

            while ((depAirport.getInQ() != 0 || plane.getInF() < SimulPar.MIN) && plane.getInF() < SimulPar.MAX) {
                if (plane.getInF() + destAirport.getPTAL() == SimulPar.N) {
                    endOp = true;
                    break;
                }
                System.out.println("hostess check documents");
                depAirport.checkDocuments();
                System.out.println("hostess wait for next passenger");
                depAirport.waitForNextPassenger();
            }

            System.out.println("hostess inform plane ready to take off");
            plane.informPlaneReadyToTakeOff();
            plane.waitForNextFlight();
        }
    }
}