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

public class Plane
{

    /**
     *  Reference to passenger threads.
     */

    private final Passenger [] passengers;

    /**
     *   Waiting queue at the transfer gate.
     */

    private MemFIFO<Integer> sitPassengers;

    /**
     *   Reference to the general repository.
     */

    private final GeneralRepos repos;

    /**
     *  Barber shop instantiation.
     *
     *    @param repos reference to the general repository
     */

    public Plane (GeneralRepos repos)
    {
        passengers = new Passenger [SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++)
            passengers[i] = null;
        try
        { sitPassengers = new MemFIFO<> (new Integer [SimulPar.MAX]);
        }
        catch (MemException e)
        { GenericIO.writelnString ("Instantiation of plane FIFO failed: " + e.getMessage ());
            sitPassengers = null;
            System.exit (1);
        }
        this.repos = repos;
    }
}
