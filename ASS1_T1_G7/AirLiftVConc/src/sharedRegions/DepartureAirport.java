package sharedRegions;

import commInfra.MemException;
import commInfra.MemFIFO;
import entities.*;
import genclass.GenericIO;
import main.SimulPar;

/**
 *    Departure Airport.
 *
 *    It is responsible to keep a continuously updated account of the entities inside the departure airport
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

public class DepartureAirport {
    /**
     * Number of passengers in queue waiting for to show their documents to the hostess.
     */

    private static int inQ;

    /**
     * Reference to passenger threads.
     */

    private final Passenger[] passengers;

    /**
     * Reference to hostess thread.
     */

    private Hostess hostess;

    /**
     * Waiting queue at the transfer gate.
     */

    private MemFIFO<Integer> boardingQueue;

    /**
     * Reference to the general repository.
     */

    private final GeneralRepos repos;

    /**
     * Departure airport instantiation.
     *
     * @param repos reference to the general repository
     */

    public DepartureAirport(GeneralRepos repos) {
        hostess = null;
        passengers = new Passenger[SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++)
            passengers[i] = null;
        try {
            boardingQueue = new MemFIFO<>(new Integer[SimulPar.N]);
        } catch (MemException e) {
            GenericIO.writelnString("Instantiation of boarding FIFO failed: " + e.getMessage());
            boardingQueue = null;
            System.exit(1);
        }
        this.repos = repos;
    }

    /**
     * Get number of passengers in queue.
     *
     * @return inQ
     */

    public static int getInQ() { return inQ; }

    /**
     * Operation prepare for pass boarding
     * <p>
     * It is called by the hostess while waiting for passengers to arrive at the airport.
     */

    public synchronized void prepareForPassBoarding() {
        int hostessId;                                          //hostess id

        hostess = (Hostess) Thread.currentThread();
        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.WAIT_FOR_PASSENGER);
        repos.setHostessState(hostessId, ((Hostess) Thread.currentThread()).getHostessState());
        hostess.setHostessCount(0);
        while (inQ == 0)                             // the hostess waits for a passenger to arrive
        {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Operation wait in queue.
     * <p>
     * It is called by a passenger while waiting for his turn to show his documents to the hostess.
     */

    public synchronized void waitInQueue() {
        int passengerId;                                      // passenger id

        passengerId = ((Passenger) Thread.currentThread()).getPassengerId();
        passengers[passengerId] = (Passenger) Thread.currentThread();
        passengers[passengerId].setPassengerState(PassengerStates.IN_QUEUE);
        repos.setPassengerState(passengerId, passengers[passengerId].getPassengerState());
        inQ++;                                        // the customer requests a hair cut service,

        try {
            boardingQueue.write(passengerId);                    // the customer sits down to wait for his turn
        } catch (MemException e) {
            GenericIO.writelnString("Insertion of customer id in waiting FIFO failed: " + e.getMessage());
            System.exit(1);
        }

        notifyAll();

        while (!(((Passenger) Thread.currentThread()).getReadyToShowDocuments())) {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }
        ((Passenger) Thread.currentThread()).setReadyToShowDocuments(false);
    }


    /**
     * Operation check documents.
     * <p>
     * It is called by the hostess while waiting for the first costumer in queue to show his documents.
     */

    public synchronized void checkDocuments() {
        int hostessId,                                          //hostess id
                passengerId;                                        //passenger id

        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.CHECK_PASSENGER);
        repos.setHostessState(hostessId, ((Hostess) Thread.currentThread()).getHostessState());

        inQ--;
        hostess.setHostessCount(hostess.getHostessCount()+1);
        hostess.setCheckedPassengers(hostess.getCheckedPassengers() + 1);
        try {
            passengerId = boardingQueue.read();                            // the hostess calls the customer
            if ((passengerId < 0) || (passengerId >= SimulPar.N))
                throw new MemException("illegal passenger id!");
        } catch (MemException e) {
            GenericIO.writelnString("Retrieval of passenger id from boarding FIFO failed: " + e.getMessage());
            passengerId = -1;
            System.exit(1);
        }

        passengers[passengerId].setReadyToShowDocuments(true);

        notifyAll();

        while (!hostess.getReadyToCheckDocuments())             // the hostess waits for the passenger to give his documents
        {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }

        hostess.setReadyToCheckDocuments(false);
    }

    /**
     * Operation show documents.
     * <p>
     * It is called by a passenger if the hostess has called him to check his documents.
     */

    public synchronized void  showDocuments() {
        hostess.setReadyToCheckDocuments(true);

        notifyAll();
        while (hostess.getHostessState() != HostessStates.WAIT_FOR_PASSENGER)   // the passenger waits until he is clear to proceed
        {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Operation wait for next passenger.
     * <p>
     * It is called by the hostess while waiting for the next passenger in queue.
     */

    /* CHANGE - Plane.getInF() + DestinationAirport.getPTAL()) - TO DO - hostess.getCheckedPassengers() */
    public synchronized void waitForNextPassenger() {
        int hostessId;                                          //hostess id

        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        ((Hostess) Thread.currentThread()).setHostessState(HostessStates.WAIT_FOR_PASSENGER);
        repos.setHostessState(hostessId, ((Hostess) Thread.currentThread()).getHostessState());

        notifyAll();
        while ((inQ == 0 && hostess.getHostessCount() < 5 || (!((Hostess) Thread.currentThread()).getReadyForNextPassenger())) && !((Plane.getInF() + DestinationAirport.getPTAL()) == SimulPar.N))    // the hostess waits for a passenger to enter the plane
        {
            try {
                wait();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
        }

        ((Hostess) Thread.currentThread()).setReadyForNextPassenger(false);
    }

    /**
     * Operation boarding the plane
     * <p>
     * It is called by the passengers when they are allowed to enter the plane.
     */

    public synchronized void boardThePlane() {
        int passengerId;                                            // passenger id

        hostess.setReadyForNextPassenger(true);
        passengerId = ((Passenger) Thread.currentThread()).getPassengerId();
        passengers[passengerId].setPassengerState(PassengerStates.IN_FLIGHT);
        repos.setPassengerState(passengerId, passengers[passengerId].getPassengerState());
        notifyAll();
    }
}