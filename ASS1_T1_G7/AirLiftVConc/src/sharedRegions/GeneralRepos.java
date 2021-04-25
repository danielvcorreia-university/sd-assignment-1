package sharedRegions;

import main.*;
import entities.*;
import genclass.GenericIO;
import genclass.TextFile;
import java.util.Objects;

/**
 *  General Repository.
 *
 *    It is responsible to keep the visible internal state of the problem and to provide means for it
 *    to be printed in the logging file.
 *    It is implemented as an implicit monitor.
 *    All public methods are executed in mutual exclusion.
 *    There are no internal synchronization points.
 */

public class GeneralRepos {
    /**
     * Name of the logging file.
     */

    private final String logFileName;

    /**
     * State of the pilot.
     */

    private int pilotState;

    /**
     * State of the hostess.
     */

    private int hostessState;

    /**
     * State of the passengers.
     */

    private final int[] passengerState;

    /**
     * Instantiation of a general repository object.
     *
     * @param logFileName name of the logging file
     */

    public GeneralRepos(String logFileName) {
        if ((logFileName == null) || Objects.equals(logFileName, ""))
            this.logFileName = "logger";
        else this.logFileName = logFileName;
        pilotState = PilotStates.AT_TRANSFER_GATE;
        hostessState = HostessStates.WAIT_FOR_FLIGHT;
        passengerState = new int[SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++)
            passengerState[i] = PassengerStates.GOING_TO_AIRPORT;
        reportInitialStatus();
    }

    /**
     * Set pilot state.
     *
     * @param id    pilot id
     * @param state pilot state
     */

    public synchronized void setPilotState(int id, int state) {
        this.pilotState = state;
        reportStatus();
    }

    /**
     * Set hostess state.
     *
     * @param id    hostess id
     * @param state hostess state
     */

    public synchronized void setHostessState(int id, int state) {
        this.hostessState = state;
        reportStatus();
    }

    /**
     * Set passenger state.
     *
     * @param id    passenger id
     * @param state passenger state
     */

    public synchronized void setPassengerState(int id, int state) {
        passengerState[id] = state;
        reportStatus();
    }

    /**
     * Write the header to the logging file.
     * <p>
     * The pilot is at transfer gate, the hostess is waiting for flight and the
     * the passengers are going to the airport.
     * Internal operation.
     */

    private void reportInitialStatus() {
        TextFile log = new TextFile();                      // instantiation of a text file handler

        if (!log.openForWriting(".", logFileName)) {
            GenericIO.writelnString("The operation of creating the file " + logFileName + " failed!");
            System.exit(1);
        }
        log.writelnString("                Airlift - Description of the internal state\n");
        log.writelnString(" PT   HT  P00  P00  P01  P02  P03  P04  P05  P06  P07  P08  " +
                "P09  P10  P11  P12  P13  P14  P15  P16  P17  P18  P19  P20 InQ InF PTAL\n");
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
        reportStatus();
    }

    /**
     * Write a state line at the end of the logging file.
     * <p>
     * The current state of the pilot, hostess and the passengers is organized in a line to be printed.
     * Internal operation.
     */

    private void reportStatus() {
        TextFile log = new TextFile();                      // instantiation of a text file handler

        String lineStatus = "";                              // state line to be printed

        if (!log.openForAppending(".", logFileName)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFileName + " failed!");
            System.exit(1);
        }
        switch (pilotState) {
            case PilotStates.AT_TRANSFER_GATE:
                lineStatus += " ATRG ";
                break;
            case PilotStates.READY_FOR_BOARDING:
                lineStatus += " RDFB ";
                break;
            case PilotStates.WAITING_FOR_BOARDING:
                lineStatus += " WTFB ";
                break;
            case PilotStates.FLYING_FORWARD:
                lineStatus += " FLFW ";
                break;
            case PilotStates.DEBOARDING:
                lineStatus += " DRPP ";
                break;
            case PilotStates.FLYING_BACK:
                lineStatus += " FLBK ";
                break;
        }
        switch (hostessState) {
            case HostessStates.WAIT_FOR_FLIGHT:
                lineStatus += " WTFL ";
                break;
            case HostessStates.WAIT_FOR_PASSENGER:
                lineStatus += " CKPS ";
                break;
            case HostessStates.CHECK_PASSENGER:
                lineStatus += " WTPS ";
                break;
            case HostessStates.READY_TO_FLY:
                lineStatus += " RDTF ";
                break;
        }
        for (int i = 0; i < SimulPar.N; i++)
            switch (passengerState[i]) {
                case PassengerStates.GOING_TO_AIRPORT:
                    lineStatus += " GTAP ";
                    break;
                case PassengerStates.IN_QUEUE:
                    lineStatus += " INQE ";
                    break;
                case PassengerStates.IN_FLIGHT:
                    lineStatus += " INFL ";
                    break;
                case PassengerStates.AT_DESTINATION:
                    lineStatus += " ATDS ";
                    break;
            }
        lineStatus = lineStatus + " " + DepartureAirport.getInQ() + " " + Plane.getInF() + " " + DestinationAirport.getPTAL();
        log.writelnString(lineStatus);
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
    }
}
