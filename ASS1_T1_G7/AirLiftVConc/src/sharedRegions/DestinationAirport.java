package sharedRegions;

import commInfra.MemException;
import commInfra.MemFIFO;
import entities.Hostess;
import entities.Passenger;
import entities.Pilot;
import genclass.GenericIO;
import main.SimulPar;

/**
 *    Destination Airport.
 *
 *    It is responsible to keep a continuously updated account of the number of passengers that are already
 *    in the destination airport.
 *    There are two methods. One to get the total number os passengers in destination airport
 *    and one to increment by one the number of passenger in destination airport.
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
     * Set number of passengers in destination by one.
     */

    public void incPTAL() {
        PTAL += 1;
    }
}
