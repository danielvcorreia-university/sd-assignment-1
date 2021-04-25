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
     *  Reference to hostess thread.
     */

    private Hostess hostess;

    /**
     *  Reference to pilot thread.
     */

    private Pilot pilot;

    /**
     *  Reference to number of passengers in the plane.
     */

    private Integer inF;

    /**
     *  Reference to passenger threads.
     */

    private final Passenger [] passengers;

    /**
     *   Waiting queue of the passengers a board to destination airport.
     */

    private MemFIFO<Integer> passengersABoard;

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
        this.inF = 0;
        passengers = new Passenger [SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++)
            passengers[i] = null;
        try
        { passengersABoard = new MemFIFO<> (new Integer [SimulPar.MAX]);
        }
        catch (MemException e)
        { GenericIO.writelnString ("Instantiation of plane FIFO failed: " + e.getMessage ());
            passengersABoard = null;
            System.exit (1);
        }
        this.repos = repos;
    }

    /**
     *  Operation wait for all passengers to board the plane.
     *
     *  It is called by the pilot after he announced the hostess
     *  that the plane is ready for boarding .
     *
     */

    public synchronized void waitForAllInBoarding ()
    {
        int pilotId;

        pilotId = ((Pilot) Thread.currentThread ()).getPilotId();
        this.pilot = (Pilot) Thread.currentThread();
        this.pilot.setPilotState(PilotStates.WAITING_FOR_BOARDING);
        repos.setPilotState (pilotId, this.pilot.getPilotState ());

        while (this.hostess.getHostessState() != HostessStates.READY_TO_FLY)
        { try
        { wait ();
        }
        catch (InterruptedException e)
        {   GenericIO.writelnString ("While waiting for passenger boarding: " + e.getMessage ());
            System.exit (1);
        }
        }
    }

    /**
     *  Operation inform the pilot that the plane is ready to departure.
     *
     *  It is called by the hostess when she ended the check in of the passengers.
     *
     */

    public synchronized void informPlaneReadyToTakeOff ()
    {
        int hostessId;

        hostessId = ((Hostess) Thread.currentThread()).getHostessId();
        this.hostess = (Hostess) Thread.currentThread();
        this.hostess.setHostessState(HostessStates.READY_TO_FLY);
        repos.setHostessState (hostessId, this.hostess.getHostessState());

        notifyAll ();
    }

    /**
     *  Operation boarding the plane
     *
     *  It is called by the passengers when they are allowed to enter the plane.
     *
     */

    public synchronized void boardThePlane ()
    {
        int passengerId;                                            // passenger id

        this.inF += 1;

        passengerId = ((Passenger) Thread.currentThread ()).getPassengerId ();
        passengers[passengerId] = (Passenger) Thread.currentThread ();
        passengers[passengerId].setPassengerState (PassengerStates.IN_FLIGHT);
        repos.setPassengerState (passengerId, passengers[passengerId].getPassengerState ());

        try
        { passengersABoard.write (passengerId);                     // the passenger sits down on plane and waits
        }
        catch (MemException e)
        { GenericIO.writelnString ("Insertion of passenger id in plane waiting FIFO failed: " + e.getMessage ());
            System.exit (1);
        }
    }

    /**
     *  Operation wait for end of flight
     *
     *  It is called by the passengers when they are inside the plane and begin their waiting journey.
     *
     */

    public synchronized void waitForEndOfFlight ()
    {
        while ((pilot.getPilotState () != PilotStates.DEBOARDING))
        { try
        { wait ();
        }
        catch (InterruptedException e)
        {
        }
        }
    }

    /**
     *  Operation inform the pilot that the plane is ready to departure.
     *
     *  It is called by the hostess when she ended the check in of the passengers.
     *
     */

    public synchronized void announceArrival ()
    {
        int pilotId;

        pilotId = ((Pilot) Thread.currentThread ()).getPilotId();
        this.pilot = (Pilot) Thread.currentThread();
        this.pilot.setPilotState(PilotStates.DEBOARDING);
        repos.setPilotState (pilotId, this.pilot.getPilotState ());

        notifyAll ();

        while (this.inF != 0)
        {
            try
            { wait ();
            }
            catch (InterruptedException e) {}
        }
    }

    /**
     *  Operation boarding the plane
     *
     *  It is called by the passengers when they are allowed to enter the plane.
     *
     */

    public synchronized void leaveThePlane ()
    {
        int passengerId;                                            // passenger id

        this.inF -= 1;

        passengerId = ((Passenger) Thread.currentThread ()).getPassengerId ();
        passengers[passengerId].setPassengerState (PassengerStates.AT_DESTINATION);
        repos.setPassengerState (passengerId, passengers[passengerId].getPassengerState ());

        try
        { passengersABoard.read ();                     // the passenger leaves the plane
        }
        catch (MemException e)
        { GenericIO.writelnString ("Removal of passenger id in plane waiting FIFO failed: " + e.getMessage ());
            System.exit (1);
        }

        if (this.inF == 0) {
            notifyAll();
        }
    }
}
