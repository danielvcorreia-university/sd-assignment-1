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

public class Pilot extends Thread
{
    /**
     *  Pilot identification.
     */

    private int pilotId;

    /**
     *  Pilot state.
     */

    private int pilotState;

    /**
     *  Reference to the departure airport.
     */

    private final DepartureAirport depAirport;

    /**
     *  Reference to the plane.
     */

    private final Plane plane;

    /**
     *  Reference to the destination airport.
     */

    private final DestinationAirport destAirport;

    /**
     *   Instantiation of a pilot thread.
     *
     *     @param name thread name
     *     @param pilotId pilot id
     *     @param depAirport reference to the departure airport
     *     @param plane reference to the plane
     */

    public Pilot (String name, int pilotId, DepartureAirport depAirport, Plane plane, DestinationAirport destAirport)
    {
        super (name);
        this.pilotId = pilotId;
        pilotState = PilotStates.AT_TRANSFER_GATE;
        this.depAirport = depAirport;
        this.plane = plane;
        this.destAirport = destAirport;
    }

    /**
     *   Set pilot id.
     *
     *     @param id pilot id
     */

    public void setPilotId (int id)
    {
        pilotId = id;
    }

    /**
     *   Get pilot id.
     *
     *     @return pilot id
     */

    public int getPilotId ()
    {
        return pilotId;
    }

    /**
     *   Set pilot state.
     *
     *     @param state new pilot state
     */

    public void setPilotState (int state)
    {
        pilotState = state;
    }

    /**
     *   Get pilot state.
     *
     *     @return pilot state
     */

    public int getPilotState ()
    {
        return pilotState;
    }

    /**
     *   Life cycle of the pilot.
     */

    @Override
    public void run ()
    {
        boolean endOp = false;                                       // flag signaling end of operations

        depAirport.parkAtTransferGate();
        while(!endOp)
        {
            depAirport.informPlaneReadyForBoarding();
            plane.waitForAllInBoarding();
            flyToDestinationPoint();
            plane.announceArrival();
            flyToDeparturePoint();
            depAirport.parkAtTransferGate();
            if (plane.getInF() + destAirport.getPTAL() == SimulPar.N)
            {
                endOp = true;
            }
        }
    }

    /**
     *  Flying the plane to the destination airport.
     *
     *  Internal operation.
     */

    private void flyToDestinationPoint ()
    {
        try
        { sleep ((long) (1 + 60 * Math.random ()));
        }
        catch (InterruptedException e)
        { GenericIO.writelnString ("Interruption: " + e.getMessage ());
            System.exit (1);
        }
    }

    /**
     *  Flying the plane to the departure airport.
     *
     *  Internal operation.
     */

    private void flyToDeparturePoint ()
    {
        try
        { sleep ((long) (1 + 57 * Math.random ()));
        }
        catch (InterruptedException e)
        { GenericIO.writelnString ("Interruption: " + e.getMessage ());
            System.exit (1);
        }
    }
}
