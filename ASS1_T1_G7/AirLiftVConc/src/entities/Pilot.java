package entities;

import genclass.GenericIO;
import main.SimulPar;
import sharedRegions.DepartureAirport;
import sharedRegions.DestinationAirport;
import sharedRegions.Plane;

/**
 *   Pilot thread.
 *
 *   It simulates the pilot life cycle.
 *   Static solution.
 */

public class Pilot extends Thread {
    /**
     * Pilot identification.
     */

    private int pilotId;

    /**
     * Pilot state.
     */

    private int pilotState;

    /**
     * True if the plane is ready to take off.
     */

    private boolean readyToTakeOff;

    /**
     * Reference to the plane.
     */

    private final Plane plane;

    /**
     * Reference to the destination airport.
     */

    private final DestinationAirport destAirport;

    /**
     * Instantiation of a pilot thread.
     *
     * @param name       thread name
     * @param pilotId    pilot id
     * @param plane      reference to the plane
     * @param destAirport reference to the destination airport
     */

    public Pilot(String name, int pilotId, Plane plane, DestinationAirport destAirport) {
        super(name);
        this.readyToTakeOff = false;
        this.pilotId = pilotId;
        pilotState = PilotStates.AT_TRANSFER_GATE;
        this.plane = plane;
        this.destAirport = destAirport;
    }

    /**
     * Set pilot id.
     *
     * @param id pilot id
     */

    public void setPilotId(int id) {
        pilotId = id;
    }

    /**
     * Get pilot id.
     *
     * @return pilot id
     */

    public int getPilotId() {
        return pilotId;
    }

    /**
     * Set if hostess has informed the pilot that the plane is ready to take off.
     *
     * @param bool ready to take off
     */

    public void setReadyToTakeOff(boolean bool) {
        readyToTakeOff = bool;
    }

    /**
     * Get ready to take off
     *
     * @return True if ready to take off
     */

    public boolean getReadyToTakeOff() {
        return readyToTakeOff;
    }

    /**
     * Set pilot state.
     *
     * @param state new pilot state
     */

    public void setPilotState(int state) {
        pilotState = state;
    }

    /**
     * Get pilot state.
     *
     * @return pilot state
     */

    public int getPilotState() {
        return pilotState;
    }

    /**
     * Life cycle of the pilot.
     */

    @Override
    public void run() {
        boolean endOp = false;                                       // flag signaling end of operations

        plane.parkAtTransferGate();
        while (!endOp) {
            plane.informPlaneReadyForBoarding();
            plane.waitForAllInBoarding();
            flyToDestinationPoint();
            plane.announceArrival();
            flyToDeparturePoint();
            plane.parkAtTransferGate();
            if (DestinationAirport.getPTAL() == SimulPar.N) {
                plane.reportFinalReport();
                endOp = true;
            }
        }
    }

    /**
     * Flying the plane to the destination airport.
     * <p>
     * Internal operation. Sleeps for an amount of time.
     */

    private void flyToDestinationPoint() {
        try {
            sleep((long) (1 + 160 * Math.random()));
        } catch (InterruptedException e) {
            GenericIO.writelnString("Interruption: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Flying the plane to the departure airport.
     * <p>
     * Internal operation. Sleeps for an amount of time.
     */

    private void flyToDeparturePoint() {
        try {
            sleep((long) (1 + 149 * Math.random()));
        } catch (InterruptedException e) {
            GenericIO.writelnString("Interruption: " + e.getMessage());
            System.exit(1);
        }
    }
}
