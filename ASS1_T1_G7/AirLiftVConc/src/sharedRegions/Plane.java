package sharedRegions;

import main.*;
import entities.*;
import commInfra.*;
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
     *
     */

    public static void setInF(int n) {
       inF = n;
    }

    /**
     * Operation prepare for pass boarding
     * <p>
     * It is called by the hostess while waiting for passengers to arrive at the airport.
     */


    public synchronized void parkAtTransferGate() {
        int pilotId;                                          //hostess id

        System.out.println("11111111");
        pilot = (Pilot) Thread.currentThread();
        pilotId = ((Pilot) Thread.currentThread()).getPilotId();
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.AT_TRANSFER_GATE);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
    }

    /**
     * Operation inform plane ready for boarding
     * <p>
     * It is called by the pilot to inform the hostess that the plane is ready for boarding.
     */


    public synchronized void informPlaneReadyForBoarding() {
        int pilotId;                                          //hostess id

        while (hostess == null) {
            System.out.println("Hostess is not initialized");
            continue;
        }
        hostess.setReadyForNextFlight(true);
        pilotId = ((Pilot) Thread.currentThread()).getPilotId();
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.READY_FOR_BOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
        notifyAll();
    }

    /**
     * Operation wait for next flight
     * <p>
     * It is called by the hostess while waiting for plane to be ready for boarding.
     */

    /* to change */
    public synchronized void waitForNextFlight() {
        int hostessId;                                          //hostess id

        hostess = (Hostess) Thread.currentThread();
        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.WAIT_FOR_FLIGHT);
        repos.setHostessState(hostessId, ((Hostess) Thread.currentThread()).getHostessState());

        System.out.println("22222222");
        if (!(getInF() + DestinationAirport.getPTAL() == SimulPar.N)) {
            System.out.println("?????????????");
            while (!(((Hostess) Thread.currentThread()).getReadyForNextFlight()))          // the hostess waits for pilot signal
            {
                try {
                    wait();
                } catch (InterruptedException e) {
                    GenericIO.writelnString("Interruption: " + e.getMessage());
                    System.exit(1);
                }
            }
        }
        ((Hostess) Thread.currentThread()).setReadyForNextFlight(false);
    }

    /**
     * Operation wait for all passengers to board the plane.
     * <p>
     * It is called by the pilot after he announced the hostess
     * that the plane is ready for boarding .
     */

    public synchronized void waitForAllInBoarding() {
        int pilotId;

        pilotId = ((Pilot) Thread.currentThread()).getPilotId();
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.WAITING_FOR_BOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());
        while (!((Pilot) Thread.currentThread()).getReadyToTakeOff()) {
            try {
                System.out.println("I'M WAKING UP");
                System.out.println(hostess.getHostessState());
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("While waiting for passenger boarding: " + e.getMessage());
                System.exit(1);
            }
        }
        System.out.println("TO ASH AND DUST");
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
        System.out.println("end of take off???");
    }

    /**
     * Operation wait for end of flight
     * <p>
     * It is called by the passengers when they are inside the plane and begin their waiting journey.
     */

    /* to change */
    public synchronized void waitForEndOfFlight() {
        int passengerId;                                            // passenger id

        passengerId = ((Passenger) Thread.currentThread()).getPassengerId();
        passengers[passengerId] = (Passenger) Thread.currentThread();

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

    /* TO CHANGE */
    public synchronized void announceArrival() {
        int pilotId;

        pilotId = ((Pilot) Thread.currentThread()).getPilotId();
        ((Pilot) Thread.currentThread()).setPilotState(PilotStates.DEBOARDING);
        repos.setPilotState(((Pilot) Thread.currentThread()).getPilotState());

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
     * Operation boarding the plane
     * <p>
     * It is called by the passengers when they are allowed to enter the plane.
     */

    public synchronized void leaveThePlane() {
        int passengerId;                                            // passenger id

        inF -= 1;

        passengerId = ((Passenger) Thread.currentThread()).getPassengerId();
        passengers[passengerId].setPassengerState(PassengerStates.AT_DESTINATION);
        repos.setPassengerState(passengerId, passengers[passengerId].getPassengerState());

        if (inF == 0) {
            notifyAll();
        }
    }
}
