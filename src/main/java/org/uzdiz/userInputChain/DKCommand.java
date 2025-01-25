package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.user.User;

public class DKCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^DK(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        if (!validateInput(input)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format naredbe. Očekuje se format 'DK ime prezime' (npr. 'DK Pero Kos').");
            return;
        }

        String[] parts = input.substring(3).trim().split(" ");
        String ime = parts[0];
        String prezime = parts[1];

        if (isUserAlreadyExists(ime, prezime)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Korisnik s imenom " + ime + " i prezimenom " + prezime + " već postoji u sustavu.");
            return;
        }

        User user = new User(ime, prezime);
        ConfigManager.getInstance().addUser(user);
        System.out.println("Korisnik " + user.getIme() + " " + user.getPrezime() + " je uspješno dodan.");
    }

    private boolean validateInput(String input) {
        return input.matches("^DK \\S+ \\S+$");
    }

    private boolean isUserAlreadyExists(String ime, String prezime) {
        return ConfigManager.getInstance().getUsers().stream()
                .anyMatch(user -> user.getIme().equalsIgnoreCase(ime) && user.getPrezime().equalsIgnoreCase(prezime));
    }
}
