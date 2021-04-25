package sharedRegions;

import main.*;
import entities.*;
import genclass.GenericIO;
import genclass.TextFile;
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

    public GeneralRepos (String logFileName)
    {
        if ((logFileName == null) || Objects.equals (logFileName, ""))
            this.logFileName = "logger";
        else this.logFileName = logFileName;
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


    public synchronized void setInfoVoo(int nVoo, int npassageiros) {
        this.ndoVoo = nVoo;
        informacaoDosVoos[nVoo-1] = nVoo + ":" + npassageiros;
    }




    private void reportInitialStatus() {
        TextFile log = new TextFile(); // instantiation of a text file handler

        if (!log.openForWriting(".", logFileName)) {
            GenericIO.writelnString("The operation of creating the file " + logFileName + " failed!");
            System.exit(1);
        }
        log.writelnString("                                                                     Problem of AirLift ");
        log.writelnString("");
        log.writelnString(
                "  PT    HT    P00   P01   P02   P03   P04   P05   P06   P07   P08   P09   P10   P11   P12   P13   P14   P15   P16   P17   P18   P19   P20  InQ   InF  PTAL");
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
        reportStatus();
    }



    private void reportStatus() {
        TextFile log = new TextFile(); // instantiation of a text file handler

        String lineStatus = ""; // state line to be printed

        if (!log.openForAppending(".", logFileName)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFileName + " failed!");
            System.exit(1);
        }

        switch (pilotState) {
            case PilotStates.AT_TRANSFER_GATE:
                lineStatus += " ATRG ";
                pilotAnteriorState = PilotStates.AT_TRANSFER_GATE;
                break;
            case PilotStates.READY_FOR_BOARDING:
                lineStatus += " RDFB ";
                log.writelnString("\nFlight " + numeroDeVoo + " : boarding started.");
                pilotAnteriorState = PilotStates.READY_FOR_BOARDING;
                break;
            case PilotStates.WAITING_FOR_BOARDING:
                lineStatus += " WTFB ";
                pilotAnteriorState = PilotStates.WAITING_FOR_BOARDING;
                break;
            case PilotStates.FLYING_FORWARD:
                lineStatus += " FLFW ";
                pilotAnteriorState = PilotStates.FLYING_FORWARD;
                break;
            case PilotStates.DEBOARDING:
                lineStatus += " DRPP ";
                if (pilotAnteriorState == PilotStates.FLYING_FORWARD) log.writelnString("\nFlight " + numeroDeVoo + " : arrived.");
                pilotAnteriorState = PilotStates.DEBOARDING;
                break;
            case PilotStates.FLYING_BACK:
                lineStatus += " FLBK ";
                if (pilotAnteriorState == PilotStates.DEBOARDING) {
                    log.writelnString("\nFlight " + numeroDeVoo + " : returning.");
                    numeroDeVoo++;
                }
                pilotAnteriorState = PilotStates.FLYING_BACK;
                break;
        }

        switch (hostessState) {
            case HostessStates.WAIT_FOR_FLIGHT:
                lineStatus += " WTFL ";
                hostessAnteriorState = HostessStates.WAIT_FOR_FLIGHT;
                break;
            case HostessStates.WAIT_FOR_PASSENGER:
                lineStatus += " WTPS ";
                hostessAnteriorState = HostessStates.WAIT_FOR_PASSENGER;
                break;
            case HostessStates.CHECK_PASSENGER:
                lineStatus += " CKPS ";
                if (hostessAnteriorState == HostessStates.WAIT_FOR_PASSENGER) {
                    //InQ--;

                }
                hostessAnteriorState = HostessStates.CHECK_PASSENGER;
                break;
            case HostessStates.READY_TO_FLY:
                lineStatus += " RDTF ";
                if (hostessAnteriorState == HostessStates.WAIT_FOR_PASSENGER) {
                    log.writelnString("\nFlight " + numeroDeVoo + " : departed with " + InF + " passengers.");
                }
                hostessAnteriorState = HostessStates.READY_TO_FLY;
                break;
        }

        for (int i = 0; i < SimulPar.N; i++)
            switch (passengerState[i]) {
                case PassengerStates.GOING_TO_AIRPORT:
                    lineStatus += " GTAP ";
                    passAnteriorState[i] = PassengerStates.GOING_TO_AIRPORT;
                    break;
                case PassengerStates.IN_QUEUE:
                    lineStatus += " INQE ";
                    if (passAnteriorState[i] == PassengerStates.GOING_TO_AIRPORT) {
                        InQ++;
                    }
                    passAnteriorState[i] = PassengerStates.IN_QUEUE;
                    break;
                case PassengerStates.IN_FLIGHT:
                    lineStatus += " INFL ";
                    if (passAnteriorState[i] == PassengerStates.IN_QUEUE) {
                        InQ--;
                        InF++;
                        log.writelnString("\nFlight " + numeroDeVoo + " : passenger " + passageiroAtual + " checked.");
                    }
                    passAnteriorState[i] = PassengerStates.IN_FLIGHT;
                    break;
                case PassengerStates.AT_DESTINATION:
                    lineStatus += " ATDS ";
                    if (passAnteriorState[i] == PassengerStates.IN_FLIGHT) {
                        InF--;
                        PTAL++;
                    }
                    passAnteriorState[i] = PassengerStates.AT_DESTINATION;
                    break;
            }

        lineStatus += "  " + InQ + "     " + InF + "     " + PTAL;
        log.writelnString(lineStatus);
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }

    }

    public synchronized void resumoDoPrograma() {
        TextFile log = new TextFile(); // instantiation of a text file handler

        String lineStatus = ""; // state line to be printed

        if (!log.openForAppending(".", logFileName)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFileName + " failed!");
            System.exit(1);
        }

        log.writelnString("\nAirlift sum up:");
        for(int i=0; i<ndoVoo; i++) {
            String[] aux = informacaoDosVoos[i].split(":");
            log.writelnString("\nFlight " + aux[0] + " transported " + aux[1] + " passengers." );
        }
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFileName + " failed!");
            System.exit(1);
        }
    }

}