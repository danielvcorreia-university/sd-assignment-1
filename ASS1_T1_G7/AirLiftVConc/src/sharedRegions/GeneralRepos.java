package sharedRegions;

import main.*;
import entities.*;
import genclass.GenericIO;
import genclass.TextFile;

import java.util.List;
import java.util.Objects;

/**
 * General Repository.
 *
 * It is responsible to keep the visible internal state of the problem and to
 * provide means for it to be printed in the logging file. It is implemented as
 * an implicit monitor. All public methods are executed in mutual exclusion.
 * There are no internal synchronization points.
 */

public class GeneralRepos {
    /**
     * Name of the logging file.
     */

    private String logFileName;

    /**
     * State of the passengers
     */

    private int[] passengerState;

    /**
     * State of the hostess
     */

    private int hostessState;

    /**
     * State of the pilot.
     */

    private int pilotState;

    /**
     * Instantiation of a general repository object.
     *
     * @param logFileName name of the logging file
     */

    private int InQ; // numero passageiro na fila
    private int InF; // numero passageiros no aviao
    private int PTAL; // numero de passageiros que ja chegaram ao destino

    private int[] passAnteriorState;
    private int pilotAnteriorState;
    private int hostessAnteriorState;

    private int numeroDeVoo;
    private int ndoVoo;
    private String[] informacaoDosVoos;
    private int passageiroAtual;
    private int [] passengerPerFlight;

    public GeneralRepos (String logFileName)
    {
        if ((logFileName == null) || Objects.equals (logFileName, ""))
            this.logFileName = "logger";
        else this.logFileName = logFileName;
        passengerPerFlight = new int  [(SimulPar.N / SimulPar.MIN)+1];
        passengerState = new int [SimulPar.N+1];
        passAnteriorState = new int [SimulPar.N+1];
        for (int i = 0; i < SimulPar.N; i++) {
            passengerState[i] = PassengerStates.GOING_TO_AIRPORT;
            passAnteriorState[i] = 0;
        }

        hostessState = HostessStates.WAIT_FOR_FLIGHT;
        hostessAnteriorState = 0;
        pilotState = PilotStates.AT_TRANSFER_GATE;
        pilotAnteriorState = 0;

        this.InQ = 0;
        this.InF = 0;
        this.PTAL = 0;

        numeroDeVoo = 1;
        passageiroAtual = 0;
        informacaoDosVoos = new String[10];


        reportInitialStatus ();

    }

    /**
     * Set passenger state.
     *
     * @param id    passenger id
     * @param state passenger state
     */

    public synchronized void setPassengerState(int id, int state) {
        this.passageiroAtual = id;
        this.passengerState[id] = state;
        reportStatus();
    }

    /**
     * Set hostess state.
     *
     * @param idHostess unique identifier of hostess
     * @param state hostess state
     */

    public synchronized void setHostessState(int idHostess, int state) {
        hostessState = state;
        reportStatus();
    }

    /**
     * Set pilot state.
     *
     * @param state pilot state
     */

    public synchronized void setPilotState(int state) {
        pilotState = state;
        reportStatus();
    }

    /**
     * Set Info of the flight.
     *
     * @param nVoo number of the flight since the begin.
     * @param npassageiros number of passengers a board.
     */

    public synchronized void setInfoVoo(int nVoo, int npassageiros) {
        this.ndoVoo = nVoo;
        informacaoDosVoos[nVoo-1] = nVoo + ":" + npassageiros;
    }

    /**
     * Inicial status of the General Repository
     *
     */

    private void reportInitialStatus() {
        TextFile log = new TextFile(); // instantiation of a text file handler

        if (!log.openForWriting(".", logFileName)) {
            GenericIO.writelnString("The operation of creating the file " + logFileName + " failed!");
            System.exit(1);
        }
        log.writelnString("                                          Airlift - Description of the internal state");
        log.writelnString("");
        log.writelnString(
                " PT   HT   P00  P01  P02  P03  P04  P05  P06  P07  P08  P09  P10  P11  P12  P13  P14  P15  P16  P17  P18  P19  P20 InQ InF PTAL");
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
    }

    /**
     * Report the status of the General Repository
     * <p>
     * It prints the current states of the hostess, pilot and passengers when one of them changes states.
     */

    private void reportStatus() {
        TextFile log = new TextFile(); // instantiation of a text file handler

        String lineStatus = ""; // state line to be printed

        if (!log.openForAppending(".", logFileName)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFileName + " failed!");
            System.exit(1);
        }

        switch (pilotState) {
            case PilotStates.AT_TRANSFER_GATE:
                lineStatus += "ATRG ";
                pilotAnteriorState = PilotStates.AT_TRANSFER_GATE;
                break;
            case PilotStates.READY_FOR_BOARDING:
                lineStatus += "RDFB ";
                if (pilotAnteriorState == PilotStates.AT_TRANSFER_GATE) log.writelnString("\nFlight " + numeroDeVoo + ": boarding started.");
                pilotAnteriorState = PilotStates.READY_FOR_BOARDING;
                break;
            case PilotStates.WAITING_FOR_BOARDING:
                lineStatus += "WTFB ";
                pilotAnteriorState = PilotStates.WAITING_FOR_BOARDING;
                break;
            case PilotStates.FLYING_FORWARD:
                lineStatus += "FLFW ";
                pilotAnteriorState = PilotStates.FLYING_FORWARD;
                break;
            case PilotStates.DEBOARDING:
                lineStatus += "DRPP ";
                if (pilotAnteriorState == PilotStates.FLYING_FORWARD) log.writelnString("\nFlight " + numeroDeVoo + ": arrived.");
                pilotAnteriorState = PilotStates.DEBOARDING;
                break;
            case PilotStates.FLYING_BACK:
                lineStatus += "FLBK ";
                if (pilotAnteriorState == PilotStates.DEBOARDING) {
                    log.writelnString("\nFlight " + numeroDeVoo + ": returning.");
                    numeroDeVoo++;
                }
                pilotAnteriorState = PilotStates.FLYING_BACK;
                break;
        }

        switch (hostessState) {
            case HostessStates.WAIT_FOR_FLIGHT:
                lineStatus += "WTFL ";
                hostessAnteriorState = HostessStates.WAIT_FOR_FLIGHT;
                break;
            case HostessStates.WAIT_FOR_PASSENGER:
                lineStatus += "WTPS ";
                hostessAnteriorState = HostessStates.WAIT_FOR_PASSENGER;
                break;
            case HostessStates.CHECK_PASSENGER:
                lineStatus += "CKPS ";
                if (hostessAnteriorState == HostessStates.WAIT_FOR_PASSENGER) {
                    //InQ--;

                }
                hostessAnteriorState = HostessStates.CHECK_PASSENGER;
                break;
            case HostessStates.READY_TO_FLY:
                lineStatus += "RDTF ";
                if (hostessAnteriorState == HostessStates.WAIT_FOR_PASSENGER) {
                    log.writelnString("\nFlight " + numeroDeVoo + ": departed with " + InF + " passengers.");
                    passengerPerFlight[numeroDeVoo-1] = InF;
                }
                hostessAnteriorState = HostessStates.READY_TO_FLY;
                break;
        }

        for (int i = 0; i < SimulPar.N; i++)
            switch (passengerState[i]) {
                case PassengerStates.GOING_TO_AIRPORT:
                    lineStatus += "GTAP ";
                    passAnteriorState[i] = PassengerStates.GOING_TO_AIRPORT;
                    break;
                case PassengerStates.IN_QUEUE:
                    lineStatus += "INQE ";
                    if (passAnteriorState[i] == PassengerStates.GOING_TO_AIRPORT) {
                        InQ++;
                    }
                    passAnteriorState[i] = PassengerStates.IN_QUEUE;
                    break;
                case PassengerStates.IN_FLIGHT:
                    lineStatus += "INFL ";
                    if (passAnteriorState[i] == PassengerStates.IN_QUEUE) {
                        InQ--;
                        InF++;
                        log.writelnString("\nFlight " + numeroDeVoo + ": passenger " + passageiroAtual + " checked.");
                    }
                    passAnteriorState[i] = PassengerStates.IN_FLIGHT;
                    break;
                case PassengerStates.AT_DESTINATION:
                    lineStatus += "ATDS ";
                    if (passAnteriorState[i] == PassengerStates.IN_FLIGHT) {
                        InF--;
                        PTAL++;
                    }
                    passAnteriorState[i] = PassengerStates.AT_DESTINATION;
                    break;
            }
        if (InQ > 9)
            lineStatus += " " + InQ;
        else
            lineStatus += "  " + InQ;

        if (InF > 9)
            lineStatus += "  " + InF;
        else
            lineStatus += "   " + InF;

        if (PTAL > 9)
            lineStatus += "  " + PTAL;
        else
            lineStatus += "   " + PTAL;

        log.writelnString(lineStatus);
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
    }

    /**
     * Report the final report of the General Repository when the pilot ended all the flights
     * <p>
     * It prints all the flights performed and the amount of passengers that were in each one
     */

    public synchronized void reportFinalInfo() {
        TextFile log = new TextFile(); // instantiation of a text file handler

        String lineStatus = ""; // state line to be printed

        if (!log.openForAppending(".", logFileName)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFileName + " failed!");
            System.exit(1);
        }

        lineStatus += "\nAirlift sum up:";
        for (int i = 0; i < passengerPerFlight.length; i++) {
            if (passengerPerFlight[i] != 0)
            { lineStatus += "\nFlight " + (i+1) + " transported " + passengerPerFlight[i] + " passengers"; }
        }
        lineStatus += ".";

        log.writelnString(lineStatus);
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
    }
}