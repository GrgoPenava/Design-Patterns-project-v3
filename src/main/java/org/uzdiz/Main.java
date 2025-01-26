package org.uzdiz;

import org.uzdiz.command.Receiver;
import org.uzdiz.mediator.NOTCommand;
import org.uzdiz.readerFactory.*;
import org.uzdiz.userInputChain.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ConfigManager config = ConfigManager.getInstance();

        parseCommandLineArgs(args, config);

        if (!validateConfig(config)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Svi argumenti (--zs, --zps, --zk, --zvr, --zod) su obavezni.");
            return;
        }

        loadDataFromCsv(config);

        CommandHandlerChain commandChain = createCommandChain(config);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Unesite komandu: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equals("Q")) {
                break;
            }

            commandChain.handleCommand(userInput);
        }
        scanner.close();
    }

    private static void parseCommandLineArgs(String[] args, ConfigManager config) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--zs":
                    if (i + 1 < args.length) {
                        config.setStationFilePath(args[++i]);
                    } else {
                        config.incrementErrorCount();
                        System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje putanja za --zs opciju");
                    }
                    break;
                case "--zps":
                    if (i + 1 < args.length) {
                        config.setRailwayFilePath(args[++i]);
                    } else {
                        config.incrementErrorCount();
                        System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje putanja za --zps opciju");
                    }
                    break;
                case "--zk":
                    if (i + 1 < args.length) {
                        config.setCompositionFilePath(args[++i]);
                    } else {
                        config.incrementErrorCount();
                        System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje putanja za --zk opciju");
                    }
                    break;
                case "--zvr":
                    if (i + 1 < args.length) {
                        config.setTimeTableFilePath(args[++i]);
                    } else {
                        config.incrementErrorCount();
                        System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje putanja za --zvr opciju");
                    }
                    break;
                case "--zod":
                    if (i + 1 < args.length) {
                        config.setDrivingDaysFilePath(args[++i]);
                    } else {
                        config.incrementErrorCount();
                        System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje putanja za --zod opciju");
                    }
                    break;
                default:
                    config.incrementErrorCount();
                    System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nepoznata opcija: " + args[i]);
            }
        }
    }

    private static boolean validateConfig(ConfigManager config) {
        return config.getStationFilePath() != null &&
                config.getRailwayFilePath() != null &&
                config.getDrivingDaysFilePath() != null &&
                config.getTimeTableFilePath() != null &&
                config.getCompositionFilePath() != null;
    }

    private static void loadDataFromCsv(ConfigManager config) {
        CsvReaderCreator stationReaderCreator = new StationReaderCreator();
        stationReaderCreator.loadData(config.getStationFilePath());

        CsvReaderCreator vehicleReaderCreator = new VehicleReaderCreator();
        vehicleReaderCreator.loadData(config.getRailwayFilePath());

        CsvReaderCreator compositionReaderCreator = new CompositionReaderCreator();
        compositionReaderCreator.loadData(config.getCompositionFilePath());

        CsvReaderCreator timeTableReaderCreator = new TimeTableReaderCreator();
        timeTableReaderCreator.loadData(config.getTimeTableFilePath());

        CsvReaderCreator drivingDaysReaderCreator = new DrivingDaysReaderCreator();
        drivingDaysReaderCreator.loadData(config.getDrivingDaysFilePath());
    }

    private static CommandHandlerChain createCommandChain(ConfigManager configManager) {
        Receiver receiver = new Receiver();
        CommandHandlerChain ipHandler = new IPCommand();
        CommandHandlerChain ispHandler = new ISPCommand();
        CommandHandlerChain isi2sHandler = new ISI2SCommand();
        CommandHandlerChain ikHandler = new IKCommand();
        CommandHandlerChain ivHandler = new IVCommand();
        CommandHandlerChain ievHandler = new IEVCommand();
        CommandHandlerChain ivrvHandler = new IVRVCommand();
        CommandHandlerChain dkHandler = new DKCommand();
        CommandHandlerChain pkHandler = new PKCommand();
        CommandHandlerChain ievdHandler = new IEVDCommand();
        CommandHandlerChain dpkHandler = new DPKCommand();
        CommandHandlerChain svvHandler = new SVVCommand();
        CommandHandlerChain cvpHandler = new CVPCommand();
        CommandHandlerChain notHandler = new NOTCommand(configManager.getMediator());
        CommandHandlerChain kkpv2sHandler = new KKPV2SCommand();
        CommandHandlerChain ikkpvHandler = new IKKPVCommand();
        CommandHandlerChain psp2sHandler = new PSP2SCommand();
        CommandHandlerChain irpsHandler = new IRPSCommand();
        CommandHandlerChain sitLeaveHandler = new SitLeaveHandler(receiver);
        CommandHandlerChain ukp2sLeaveHandler = new UKP2SCommand();
        CommandHandlerChain ivi2sLeaveHandler = new IVI2SCommand();

        ipHandler.setNextHandler(ispHandler);
        ispHandler.setNextHandler(isi2sHandler);
        isi2sHandler.setNextHandler(ikHandler);
        ikHandler.setNextHandler(ivHandler);
        ivHandler.setNextHandler(ievHandler);
        ievHandler.setNextHandler(ivrvHandler);
        ivrvHandler.setNextHandler(dkHandler);
        dkHandler.setNextHandler(pkHandler);
        pkHandler.setNextHandler(ievdHandler);
        ievdHandler.setNextHandler(dpkHandler);
        dpkHandler.setNextHandler(svvHandler);
        svvHandler.setNextHandler(cvpHandler);
        cvpHandler.setNextHandler(notHandler);
        notHandler.setNextHandler(kkpv2sHandler);
        kkpv2sHandler.setNextHandler(ikkpvHandler);
        ikkpvHandler.setNextHandler(psp2sHandler);
        psp2sHandler.setNextHandler(irpsHandler);
        irpsHandler.setNextHandler(sitLeaveHandler);
        sitLeaveHandler.setNextHandler(ukp2sLeaveHandler);
        ukp2sLeaveHandler.setNextHandler(ivi2sLeaveHandler);

        return ipHandler;
    }
}