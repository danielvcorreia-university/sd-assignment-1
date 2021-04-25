package sharedRegions;

import commInfra.MemException;
import commInfra.MemFIFO;
import entities.Hostess;
import entities.Passenger;
import entities.Pilot;
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

public class DestinationAirport {
    /**
     * Number of passengers that have arrived at the destination and have left the plane.
     */

    private static int PTAL;

    /**
     * Reference to the general repository.
     */

    private final GeneralRepos repos;

    /**
     * Destination airport instantiation.
     *
     * @param repos reference to the general repository
     */

    public DestinationAirport(GeneralRepos repos) {
        PTAL = 0;
        this.repos = repos;
    }

    /**
     * Get number of passengers in destination.
     *
     * @return PTAL
     */

    public static int getPTAL() {
        return PTAL;
    }

    /**
     * Get number of passengers in destination.
     */

    public void incPTAL() {
        PTAL += 1;
    }
}
