package sharedRegions;

import commInfra.MemException;
import commInfra.MemFIFO;
import entities.Passenger;
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

public class DepartureAirport
{
    /**
     *  Number of passengers in queue waiting for to show their documents to the hostess.
     */

    private int nPassQueue;

    /**
     *  Reference to passenger threads.
     */

    private final Passenger [] passengers;

    /**
     *   Waiting queue at the transfer gate.
     */

    private MemFIFO<Integer> boardingQueue;

    /**
     *   Reference to the general repository.
     */

    private final GeneralRepos repos;

    /**
     *  Barber shop instantiation.
     *
     *    @param repos reference to the general repository
     */

    public DepartureAirport(GeneralRepos repos)
    {
        passengers = new Passenger [SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++)
            passengers[i] = null;
        try
        { boardingQueue = new MemFIFO<> (new Integer [SimulPar.N]);
        }
        catch (MemException e)
        { GenericIO.writelnString ("Instantiation of boarding FIFO failed: " + e.getMessage ());
            boardingQueue = null;
            System.exit (1);
        }
        this.repos = repos;
    }

    /**
     *  Operation go cut the hair.
     *
     *  It is called by a customer when he goes to the barber shop to try and cut his hair.
     *
     *    @return true, if he did manage to cut his hair -
     *            false, otherwise
     */

    public synchronized boolean goCutHair ()
    {
        int customerId;                                      // customer id

        customerId = ((Customer) Thread.currentThread ()).getCustomerId ();
        cust[customerId] = (Customer) Thread.currentThread ();
        cust[customerId].setCustomerState (CustomerStates.WANTTOCUTHAIR);
        repos.setCustomerState (customerId, cust[customerId].getCustomerState ());

        if (sitCustomer.full ())                             // the customer checks how full is the barber shop
            return (false);                                   // if it is packed full, he leaves to come back later

        cust[customerId].setCustomerState (CustomerStates.WAITTURN);
        repos.setCustomerState (customerId, cust[customerId].getCustomerState ());
        nReqCut += 1;                                        // the customer requests a hair cut service,

        try
        { sitCustomer.write (customerId);                    // the customer sits down to wait for his turn
        }
        catch (MemException e)
        { GenericIO.writelnString ("Insertion of customer id in waiting FIFO failed: " + e.getMessage ());
            System.exit (1);
        }

        notifyAll ();                                        // the customer lets his presence be known

        while (((Customer) Thread.currentThread ()).getCustomerState () != CustomerStates.DAYBYDAYLIFE)
        { /* the customer waits for the service to be executed */
            try
            { wait ();
            }
            catch (InterruptedException e) {}
        }

        return (true);                                       // the customer leaves the barber shop after being serviced
    }

    /**
     *  Operation wait in queue.
     *
     *  It is called by a passenger while waiting for his turn to show his documents to the hostess.
     *
     *    @return true, if his life cycle has come to an end -
     *            false, otherwise
     */


    public synchronized boolean waitInQueue()
    {
        while (nPassQueue == 0)                             // passenger waits for his turn to show his documents
        { try
        { wait ();
        }
        catch (InterruptedException e)
        { return true;
        }
        }

        if (nPassQueue > 0) nPassQueue -= 1;                // the hostess takes notice that a passenger arrived

        return false;
    }

    /**
     *  Operation go to sleep.
     *
     *  It is called by a barber while waiting for customers to be serviced.
     *
     *    @return true, if his life cycle has come to an end -
     *            false, otherwise
     */


    public synchronized boolean prepareForPassBoarding  ()
    {
        while (nPassQueue == 0)                             // the hostess waits for a passenger to arrive
        { try
        { wait ();
        }
        catch (InterruptedException e)
        { return true;
        }
        }

        if (nPassQueue > 0) nPassQueue -= 1;                // the hostess takes notice that a passenger arrived

        return false;
    }


    /**
     *  Operation go to sleep.
     *
     *  It is called by a barber while waiting for customers to be serviced.
     *
     *    @return true, if his life cycle has come to an end -
     *            false, otherwise
     */

    public synchronized boolean checkDocuments   ()
    {
        while (nReqCut == 0)                                 // the barber waits for a service request
        { try
        { wait ();
        }
        catch (InterruptedException e)
        { return true;                                     // the barber life cycle has come to an end
        }
        }

        if (nReqCut > 0) nReqCut -= 1;                       // the barber takes notice some one has requested his service

        return false;
    }

    /**
     *  Operation go to sleep.
     *
     *  It is called by a barber while waiting for customers to be serviced.
     *
     *    @return true, if his life cycle has come to an end -
     *            false, otherwise
     */

    public synchronized boolean waitForNextPassenger   ()
    {
        while (nReqCut == 0)                                 // the barber waits for a service request
        { try
        { wait ();
        }
        catch (InterruptedException e)
        { return true;                                     // the barber life cycle has come to an end
        }
        }

        if (nReqCut > 0) nReqCut -= 1;                       // the barber takes notice some one has requested his service

        return false;
    }

    /**
     *  Operation call a customer.
     *
     *  It is called by a barber if a customer has requested his service.
     *
     *    @return customer id
     */

    public synchronized int callACustomer ()
    {
        int barberId,                                                  // barber id
                customerId;                                                // customer id

        barberId = ((Barber) Thread.currentThread ()).getBarberId ();
        ((Barber) Thread.currentThread ()).setBarberState (BarberStates.INACTIVITY);
        repos.setBarberState (barberId, ((Barber) Thread.currentThread ()).getBarberState ());

        try
        { customerId = sitCustomer.read ();                            // the barber calls the customer
            if ((customerId < 0) || (customerId >= SimulPar.N))
                throw new MemException ("illegal customer id!");
        }
        catch (MemException e)
        { GenericIO.writelnString ("Retrieval of customer id from waiting FIFO failed: " + e.getMessage ());
            customerId = -1;
            System.exit (1);
        }

        cust[customerId].setCustomerState (CustomerStates.CUTTHEHAIR);
        repos.setCustomerState (customerId, cust[customerId].getCustomerState ());

        return (customerId);
    }

    /**
     *  Operation receive payment.
     *
     *  It is called by a barber after finishing the customer hair cut.
     *
     *    @param customerId customer id
     */

    public synchronized void receivePayment (int customerId)
    {
        int barberId;                                        // barber id

        barberId = ((Barber) Thread.currentThread ()).getBarberId ();
        ((Barber) Thread.currentThread ()).setBarberState (BarberStates.SLEEPING);
        cust[customerId].setCustomerState (CustomerStates.DAYBYDAYLIFE);
        repos.setBarberCustomerState (barberId, ((Barber) Thread.currentThread ()).getBarberState (),
                customerId, cust[customerId].getCustomerState ());

        notifyAll ();                                        // the customer settles the account
    }
}