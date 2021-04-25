package entities;

/**
 *    Definition of the internal states of the hostess during his life cycle.
 */

public final class HostessStates {
    /**
     * The hostess waits until the plane is ready for boarding.
     */

    public static final int WAIT_FOR_FLIGHT = 0;

    /**
     * The hostess waits until a passenger arrives at the boarding queue.
     */

    public static final int WAIT_FOR_PASSENGER = 1;

    /**
     * The hostess waits until the passenger shows his documents and checks them.
     */

    public static final int CHECK_PASSENGER = 2;

    /**
     * The hostess informs the pilot that the plane is ready to take off.
     */

    public static final int READY_TO_FLY = 3;

    /**
     * It can not be instantiated.
     */

    private HostessStates() {
    }
}