package org.uzdiz;

import org.uzdiz.mediator.NOTCommand;
import org.uzdiz.readerFactory.*;
import org.uzdiz.userInput.*;

import java.util.HashMap;
import java.util.Map;
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

        Map<String, Command> commands = new HashMap<>();

        loadCommands(commands, config);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Unesite komandu: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equals("Q")) {
                break;
            }

            Command command = commands.get(userInput.split(" ")[0]);
            if (command != null) {
                CommandExecutor executor = new CommandExecutor(command);
                executor.executeCommand(userInput);
            } else {
                config.incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nepoznata komanda. Pokušajte ponovno.");
            }
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

    private static void loadCommands(Map<String, Command> commands, ConfigManager configManager) {
        commands.put("IP", new IPCommand());
        commands.put("ISP", new ISPCommand());
        commands.put("ISI2S", new ISI2SCommand());
        commands.put("IK", new IKCommand());
        commands.put("IV", new IVCommand());
        commands.put("IEV", new IEVCommand());
        commands.put("IVRV", new IVRVCommand());
        commands.put("DK", new DKCommand());
        commands.put("PK", new PKCommand());
        commands.put("IEVD", new IEVDCommand());
        commands.put("DPK", new DPKCommand());
        commands.put("SVV", new SVVCommand());
        commands.put("NOT", new NOTCommand(configManager.getMediator()));
    }
}