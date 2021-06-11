package sharedRegions;

import main.*;
import entities.*;
import genclass.GenericIO;

/**
 *    Plane.
 *
 *    It is responsible to keep a continuously updated account of the entities inside the plane
 *    and is implemented as an implicit monitor.
 *    All public methods are executed in mutual exclusion.
 *    There are four internal synchronization points: a single blocking point for the hostess, where she waits until
 *    the plane is ready for boarding so that she may proceed to the next flight;
 *    another single blocking point for the hostess, where she waits for the passengers to arrive at the airport;
 *    another single blocking point for the hostess, where she waits for the passenger at the front of the queue to
 *    show her his documents;
 *    and an array of blocking points, one per each passenger, where he both waits his turn to show the hostess
 *    his documents and waits until she has checked his documents and calls the next passenger.
 */

public class Plane {

    /**
     * Reference to number of passengers in the plane.
     */

    private static int inF;

    /**
     * True if the pilot communicated to the hostess that the plane is ready for boarding.
     */

    private boolean nextFlight;

    /**
     * True if the plane is ready to take off per hostess signal.
     */

    private boolean readyToTakeOff;

    /**
     * True if the plane has arrived and the passengers should deboard.
     */

    private boolean startDeboarding;

    /**
     * Reference to the general repository.
     */

    private final GeneralRepos repos;

    /**
     * Plane instantiation.
     *
     * @param repos reference to the general repository
     */

    public Plane(GeneralRepos repos) {
        inF = 0;
        nextFlight = false;
        readyToTakeOff = false;
        startDeboarding = false;
        this.repos = repos;
    }

    /**
     * Operation to get number passengers in flight
     * <p>
     * It is called by the passenger when he is leaving the plane at destination airport so he can check if he is the last to leave.
     *
     * @return Number of passengers currently in flight
     */

    public synchronized int getInF() {
        return inF;
    }

    /**
     * Operation to report the final report
     * <p>
     * It is called by the pilot after he parks the plane at the transfer gate and there are no more passengers to transport
     */

    public synchronized void reportFinalReport() {
        repos.reportFinalInfo();
    }

    /**
     * Operation prepare for pass boarding
     * <p>
     * It is called by the hostess while waiting for passengers to arrive at the airport.
     */

    public synchronized void parkAtTransferGate() {

        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.AT_TRANSFER_GATE);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
    }

    /**
     * Operation inform plane ready for boarding
     * <p>
     * It is called by the pilot to inform the hostess that the plane is ready for boarding.
     */


    public synchronized void informPlaneReadyForBoarding() {
        nextFlight = true;
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.READY_FOR_BOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
        notifyAll();
    }

    /**
     * Operation wait for next flight
     * <p>
     * It is called by the hostess while waiting for plane to be ready for boarding.
     */

    public synchronized void waitForNextFlight(boolean first) {

        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.WAIT_FOR_FLIGHT);
        if(!first)
            repos.setHostessState(((Hostess) Thread.currentThread()).getHostessId(), ((Hostess) Thread.currentThread()).getHostessState());

        ((Hostess) Thread.currentThread()).setCheckedPassengers(((Hostess) Thread.currentThread()).getCheckedPassengers() + inF);

        if (!(((Hostess) Thread.currentThread()).getCheckedPassengers() == SimulPar.N)) {
            while(!nextFlight)
            {
                try {
                    wait();
                } catch (InterruptedException e) {
                    GenericIO.writelnString("Interruption: " + e.getMessage());
                    System.exit(1);
                }
            }
        }
        nextFlight = false;

    }

    /**
     * Operation wait for all passengers to board the plane.
     * <p>
     * It is called by the pilot after he announced the hostess
     * that the plane is ready for boarding .
     */

    public synchronized void waitForAllInBoarding() {
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.WAITING_FOR_BOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
        while (!readyToTakeOff) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("While waiting for passenger boarding: " + e.getMessage());
                System.exit(1);
            }
        }
        readyToTakeOff = false;
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.FLYING_FORWARD);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
    }

    /**
     * Operation inform the pilot that the plane is ready to departure.
     * <p>
     * It is called by the hostess when she ended the check in of the passengers.
     */

    public synchronized void informPlaneReadyToTakeOff() {

        readyToTakeOff = true;
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.READY_TO_FLY);
        repos.setHostessState(((Hostess) Thread.currentThread()).getHostessId(), ((Hostess) Thread.currentThread()).getHostessState());
        notifyAll();
    }

    /**
     * Operation wait for end of flight
     * <p>
     * It is called by the passengers when they are inside the plane and begin their waiting journey.
     */

    public synchronized void waitForEndOfFlight() {

        inF += 1;

        while (!startDeboarding) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Operation announce that the plane has arrived at the destination airport.
     * <p>
     * It is called by the pilot when the plane has arrived at the destination airport.
     */

    public synchronized void announceArrival() {
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.DEBOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());

        ((Pilot) Thread.currentThread()).setTransportedPassengers(((Pilot) Thread.currentThread()).getTransportedPassengers() + inF);

        startDeboarding = true;

        notifyAll();

        while (inF != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }

        startDeboarding = false;
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.FLYING_BACK);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
    }

    /**
     * Operation to notify the pilot
     * <p>
     * It is called by the last passenger when he is leaving the plane to awake the pilot who is waiting.
     */

    public synchronized void notifyPilot() {
        inF = 0;

        notifyAll();
    }
}
