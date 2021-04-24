package entities;

import sharedRegions.DepartureAirport;
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
     *   Instantiation of a pilot thread.
     *
     *     @param name thread name
     *     @param pilotId pilot id
     *     @param depAirport reference to the departure airport
     *     @param plane reference to the plane
     */

    public Pilot (String name, int pilotId, DepartureAirport depAirport, Plane plane)
    {
        super (name);
        this.pilotId = pilotId;
        pilotState = PilotStates.AT_TRANSFER_GATE;
        this.depAirport = depAirport;
        this.plane = plane;
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
        while(true)
        {	if (noMorePassagers) break;
            DepartureAirport.informPlaneReadyForBoarding();
            Plane.waitForAllInBoarding();
            Plane.flyToDestinationPoint();
            Plane.announceArrival();
            Plane.flyToDeparturePoint();
            noMorePassagers = DepartureAirport.parkAtTransferGate();
        }
    }

    /**
     *  Flying the plane to the destination airport.
     *
     *  Internal operation.
     */

    private void flyToDestinationPoint  ()
    {
        try
        { sleep ((long) (1 + 60 * Math.random ()));
        }
        catch (InterruptedException e) {}
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
        catch (InterruptedException e) {}
    }
}
