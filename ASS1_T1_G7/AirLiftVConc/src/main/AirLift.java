package main;


import entities.Hostess;
import entities.Passenger;
import entities.Pilot;
import genclass.FileOp;
import genclass.GenericIO;
import sharedRegions.DepartureAirport;
import sharedRegions.DestinationAirport;
import sharedRegions.GeneralRepos;
import sharedRegions.Plane;

/**
 *   Simulation of the Problem of the Air Lift.
 *   Static solution based on a posteriori reasoning to terminate the Air Lift threads.
 */


public class AirLift {
    /**
     * Main method.
     *
     * @param args runtime arguments
     */

    public static void main(String[] args) {
        Passenger[] passenger = new Passenger[SimulPar.N];     // array of passenger threads
        Pilot pilot;                                            // pilot thread
        Hostess hostess;                                        // reference to the barber shop
        DepartureAirport depAirport;                            // reference to the departure airport repository
        DestinationAirport desAirport;                          // reference to the destination airport repository
        Plane plane;                                            // reference to the plane repository
        GeneralRepos repos;                                     // reference to the general repository
        String fileName;                                        // logging file name
        char opt;                                               // selected option
        boolean success;                                        // end of operation flag

        /* problem initialization */

        GenericIO.writelnString("\n" + "      Problem of the Air Lift\n");
        do {
            GenericIO.writeString("Logging file name? ");
            fileName = GenericIO.readlnString();
            if (FileOp.exists(".", fileName)) {
                do {
                    GenericIO.writeString("There is already a file with this name. Delete it (y - yes; n - no)? ");
                    opt = GenericIO.readlnChar();
                } while ((opt != 'y') && (opt != 'n'));
                if (opt == 'y')
                    success = true;
                else success = false;
            } else success = true;
        } while (!success);

        repos = new GeneralRepos(fileName);
        desAirport = new DestinationAirport(repos);
        depAirport = new DepartureAirport(repos);
        plane = new Plane(repos);
        pilot = new Pilot("Pilot_" + (1), 0, plane, desAirport);
        hostess = new Hostess("Hostess_" + (1), 0, depAirport, plane, desAirport);
        for (int i = 0; i < SimulPar.N; i++) {
            passenger[i] = new Passenger("Passenger_" + (i + 1), i, depAirport, plane, desAirport);
        }

        /* start of the simulation */

        pilot.start();
        hostess.start();
        for (int i = 0; i < SimulPar.N; i++)
            passenger[i].start();

        /* waiting for the end of the simulation */

        GenericIO.writelnString();
        for (int i = 0; i < SimulPar.N; i++) {
            try {
                passenger[i].join();
            } catch (InterruptedException e) {
                GenericIO.writelnString("Interruption: " + e.getMessage());
                System.exit(1);
            }
            GenericIO.writelnString("The passenger " + (i + 1) + " has terminated.");
        }
        GenericIO.writelnString();
        while (pilot.isAlive()) {
            //pilot.interrupt();
            Thread.yield();
        }
        try {
            pilot.join();
        } catch (InterruptedException e) {
            GenericIO.writelnString("Interruption: " + e.getMessage());
            System.exit(1);
        }
        GenericIO.writelnString("The pilot " + (1) + " has terminated.");

        GenericIO.writelnString();
        while (hostess.isAlive()) {
            //hostess.interrupt();
            Thread.yield();
        }
        try {
            hostess.join();
        } catch (InterruptedException e) {
            GenericIO.writelnString("Interruption: " + e.getMessage());
            System.exit(1);
        }
        GenericIO.writelnString("The hostess " + (1) + " has terminated.");

        GenericIO.writelnString();
    }
}
