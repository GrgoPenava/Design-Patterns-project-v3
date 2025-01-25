package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.command.*;
import org.uzdiz.timeTableComposite.TimeTableComponent;
import org.uzdiz.timeTableComposite.TimeTableComposite;
import org.uzdiz.timeTableComposite.Train;
import org.uzdiz.user.User;

public class SitLeaveHandler extends CommandHandlerChain {
    private Receiver receiver;
    private CommandInvoker invoker;

    public SitLeaveHandler(Receiver receiver) {
        this.receiver = receiver;
        this.invoker = new CommandInvoker();
    }

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^(SJE|MAK|POVIJEST|UNDO|CLEAR)\\s*.*");
    }

    @Override
    protected void execute(String input) {
        if (input.equals("UNDO")) {
            undoLastCommand();
            return;
        }

        if (input.equals("POVIJEST")) {
            showHistory();
            return;
        }

        if (input.equals("CLEAR")) {
            clearHistory();
            return;
        }

        String[] parts = input.split("\\s+", 3);
        if (parts.length < 3) {
            System.out.println("Greška: Naredba mora biti u formatu 'SJE|MAK brojVlaka ime-prezime'.");
            return;
        }

        String commandType = parts[0];
        String trainId = parts[1];
        String[] userParts = parts[2].split("-");

        if (userParts.length != 2) {
            System.out.println("Greška: Ime i prezime korisnika moraju biti u formatu 'ime-prezime'.");
            return;
        }

        String ime = userParts[0];
        String prezime = userParts[1];

        Train train = findTrainById(trainId);
        if (train == null) {
            System.out.println("Greška: Vlak s oznakom '" + trainId + "' ne postoji.");
            return;
        }

        User user = findUserByName(ime, prezime);
        if (user == null) {
            System.out.println("Greška: Korisnik " + ime + " " + prezime + " nije pronađen u sustavu. Dodajte korisnika pomoću naredbe 'DK'.");
            return;
        }

        switch (commandType) {
            case "SJE" -> {
                Command sitCommand = new SitCommand(train, user, receiver);
                invoker.executeCommand(sitCommand);
            }
            case "MAK" -> {
                Command leaveCommand = new LeaveCommand(train, user, receiver);
                invoker.executeCommand(leaveCommand);
            }
            default -> System.out.println("Nepoznata naredba: " + commandType);
        }
    }

    private void undoLastCommand() {
        System.out.println("Poništavanje posljednje komande...");
        invoker.undoLastCommand();
    }

    private void showHistory() {
        System.out.println("\nPovijest ulazaka/izlazaka:");
        receiver.getHistory().forEach(System.out::println);
    }

    private Train findTrainById(String trainId) {
        TimeTableComposite timetable = ConfigManager.getInstance().getVozniRed();
        for (TimeTableComponent component : timetable.getChildren()) {
            if (component instanceof Train train && train.getOznaka().equals(trainId)) {
                return train;
            }
        }
        return null;
    }

    private User findUserByName(String ime, String prezime) {
        return ConfigManager.getInstance().getUsers().stream()
                .filter(user -> user.getIme().equalsIgnoreCase(ime) && user.getPrezime().equalsIgnoreCase(prezime))
                .findFirst()
                .orElse(null);
    }

    private void clearHistory() {
        System.out.println("Brisanje povijesti...");
        receiver.clearHistory();
    }
}