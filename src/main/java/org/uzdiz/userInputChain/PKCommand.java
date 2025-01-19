package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.user.User;
import org.uzdiz.utils.TableBuilder;

import java.util.List;

public class PKCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^PK(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        if (!validateInput(input)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format naredbe. Očekuje se 'PK'.");
            return;
        }

        List<User> users = ConfigManager.getInstance().getUsers();

        if (users.isEmpty()) {
            System.out.println("Nema registriranih korisnika.");
            return;
        }

        TableBuilder table = new TableBuilder();
        table.setHeaders("Ime i Prezime");

        for (User user : users) {
            table.addRow(user.getIme() + " " + user.getPrezime());
        }

        table.build();
    }

    private boolean validateInput(String input) {
        return input.trim().equals("PK");
    }
}
