package entities;

import sharedRegions.DepartureAirport;
import sharedRegions.Plane;

/**
 *   Hostess thread.
 *
 *   It simulates the hostess life cycle.
 *   Static solution.
 */

public class Hostess extends Thread
{
    /**
     *  Hostess identification.
     */

    private int hostessId;

    /**
     *  Hostess state.
     */

    private int hostessState;

    /**
     *  True if the passenger has given his documents to the hostess for her to check.
     */

    private boolean readyToCheckDocuments;

    /**
     *  Reference to the departure airport.
     */

    private final DepartureAirport depAirport;

    /**
     *  Reference to the plane.
     */

    private final Plane plane;

    /**
     *   Instantiation of a hostess thread.
     *
     *     @param name thread name
     *     @param hostessId hostess id
     *     @param depAirport reference to the departure airport
     *     @param plane reference to the plane
     */

    public Hostess (String name, int hostessId, DepartureAirport depAirport, Plane plane)
    {
        super (name);
        this.hostessId = hostessId;
        hostessState = HostessStates.WAIT_FOR_FLIGHT;
        this.depAirport = depAirport;
        this.plane = plane;
    }

    /**
     *   Set hostess id.
     *
     *     @param id hostess id
     */

    public void setHostessId (int id)
    {
        hostessId = id;
    }

    /**
     *   Get hostess id.
     *
     *     @return hostess id
     */

    public int getHostessId ()
    {
        return hostessId;
    }

    /**
     *   Set if hostess has received the documents from the passenger.
     *
     *     @param bool ready to check documents
     */

    public void setReadyToCheckDocuments (boolean bool)
    {
        readyToCheckDocuments = bool;
    }

    /**
     *   Get ready to check documents.
     *
     *     @return ready to check documents
     */

    public boolean getReadyToCheckDocuments ()
    {
        return readyToCheckDocuments;
    }

    /**
     *   Set hostess state.
     *
     *     @param state new hostess state
     */

    public void setHostessState (int state)
    {
        hostessState = state;
    }

    /**
     *   Get hostess state.
     *
     *     @return hostess state
     */

    public int getHostessState ()
    {
        return hostessState;
    }

    /**
     *   Life cycle of the hostess.
     */

    @Override
    public void run ()
    {
        int customerId;                                      // customer id
        boolean endOp;                                       // flag signaling end of operations

        while(true)
        {	if (noMorePassagers) break;
            depAirport.prepareForPassBoarding();

            while ( (!depAirport.isQueueEmpty() or !Plane.minPassagers()) and !Plane.maxPassagers() )
            {	if (Plane.passagersInFlight + DestinationAirport.passagersArrivedDestination == SimulationPar.N)
                break;
                depAirport.checkDocuments(first in queue);
                depAirport.waitForNextPassager();

            }

            Plane.informPlaneReadyToTakeOff();
            noMorePassagers = depAirport.waitForNextFlight();
        }
    }
}
