package entities;

/**
 *    Definition of the internal states of the pilot during his life cycle.
 */

public final class PilotStates
{
    /**
     *   The pilot parks the plane at the transfer gate.
     */

    public static final int AT_TRANSFER_GATE = 0;

    /**
     *   The pilot informs that the plane is ready for boarding.
     */

    public static final int READY_FOR_BOARDING = 1;

    /**
     *   The pilot waits until the boarding of the plane is complete.
     */

    public static final int WAITING_FOR_BOARDING = 2;

    /**
     *   The pilot flies the plane until the destination airport is reached.
     */

    public static final int FLYING_FORWARD = 3;

    /**
     *   The pilot announces that the plane has reached its destination and waits for the passengers to leave the plane.
     */

    public static final int DEBOARDING = 4;

    /**
     *   The pilot flies the plane back to the departure airport.
     */

    public static final int FLYING_BACK = 5;

    /**
     *   It can not be instantiated.
     */

    private PilotStates ()
    { }
}
