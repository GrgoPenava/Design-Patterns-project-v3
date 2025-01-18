package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;
import org.uzdiz.user.User;

public class DKCommand implements Command {

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

        User user = new User(ime, prezime);
        ConfigManager.getInstance().addUser(user);
        System.out.println("Korisnik " + user.getIme() + " " + user.getPrezime() + " je uspješno dodan.");
    }

    private boolean validateInput(String input) {
        return input.matches("^DK \\S+ \\S+$");
    }
}
