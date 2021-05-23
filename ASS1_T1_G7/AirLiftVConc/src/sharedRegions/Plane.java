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
     * Reference to hostess thread.
     */

    private Hostess hostess;

    /**
     * Reference to pilot thread.
     */

    private Pilot pilot;

    /**
     * Reference to number of passengers in the plane.
     */

    private static int inF;

    /**
     * True if the pilot communicated to the hostess that the plane is ready for boarding.
     */

    private boolean nextFlight;

    /**
     * Reference to passenger threads.
     */

    private final Passenger[] passengers;

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
        passengers = new Passenger[SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++)
            passengers[i] = null;
        this.repos = repos;
    }

    /**
     * Get number of passengers in flight.
     *
     * @return inF
     */

    public static int getInF() {
        return inF;
    }

    /**
     * Set number of passengers in flight.
     * @param n set number of passengers in flight
     *
     */

    public static void setInF(int n) {
       inF = n;
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

        pilot = (Pilot) Thread.currentThread();
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.AT_TRANSFER_GATE);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
        //if (DestinationAirport.getPTAL() == SimulPar.N) { repos.reportFinalInfo(); }
    }

    /**
     * Operation inform plane ready for boarding
     * <p>
     * It is called by the pilot to inform the hostess that the plane is ready for boarding.
     */


    public synchronized void informPlaneReadyForBoarding() {

        //while (hostess == null) {
        //    System.out.println("Hostess is not initialized");
        //}
        //hostess.setReadyForNextFlight(true);*/
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
        int hostessId;                                          //hostess id

        hostess = (Hostess) Thread.currentThread();
        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.WAIT_FOR_FLIGHT);
        if(!first)
            repos.setHostessState(hostessId, ((Hostess) Thread.currentThread()).getHostessState());

        hostess.setCheckedPassengers(hostess.getCheckedPassengers() + inF);

        if (!(hostess.getCheckedPassengers() == SimulPar.N)) {
            // while (!(((Hostess) Thread.currentThread()).getReadyForNextFlight()))          // the hostess waits for pilot signal
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
        //((Hostess) Thread.currentThread()).setReadyForNextFlight(false);
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
        while (!((Pilot) Thread.currentThread()).getReadyToTakeOff()) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("While waiting for passenger boarding: " + e.getMessage());
                System.exit(1);
            }
        }
        pilot.setReadyToTakeOff(false);
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.FLYING_FORWARD);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
    }

    /**
     * Operation inform the pilot that the plane is ready to departure.
     * <p>
     * It is called by the hostess when she ended the check in of the passengers.
     */

    public synchronized void informPlaneReadyToTakeOff() {
        int hostessId;

        pilot.setReadyToTakeOff(true);
        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.READY_TO_FLY);
        repos.setHostessState(hostessId, ((Hostess) Thread.currentThread()).getHostessState());
        notifyAll();
    }

    /**
     * Operation wait for end of flight
     * <p>
     * It is called by the passengers when they are inside the plane and begin their waiting journey.
     */

    public synchronized void waitForEndOfFlight() {
        int passengerId;                                            // passenger id

        passengerId = ((Passenger) Thread.currentThread()).getPassengerId();
        passengers[passengerId] = (Passenger) Thread.currentThread();
        inF += 1;

        while ((pilot.getPilotState() != PilotStates.DEBOARDING)) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Operation inform the pilot that the plane is ready to departure.
     * <p>
     * It is called by the hostess when she ended the check in of the passengers.
     */

    public synchronized void announceArrival() {
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.DEBOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());

        pilot.setTransportedPassengers(pilot.getTransportedPassengers() + inF);

        notifyAll();

        while (inF != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }

        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.FLYING_BACK);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
    }

    /**
     * Operation leave the plane
     * <p>
     * It is called by the passengers when they leave the plane.
     */

    public synchronized void leaveThePlane() {
        int passengerId;                                            // passenger id

        inF -= 1;

        passengerId = ((Passenger) Thread.currentThread()).getPassengerId();
        passengers[passengerId].setPassengerState(PassengerStates.AT_DESTINATION);
        repos.setPassengerState(passengerId, passengers[passengerId].getPassengerState());

        if (inF == 0) { notifyAll(); }
    }
}
