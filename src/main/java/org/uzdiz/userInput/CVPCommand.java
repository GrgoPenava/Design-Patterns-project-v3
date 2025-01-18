package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;

public class CVPCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.startsWith("CVP");
    }

    @Override
    public void execute(String input) {
        ConfigManager config = ConfigManager.getInstance();

        if (!validateInput(input)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() +
                    ": Neispravan format naredbe. Očekuje se 'CVP cijenaNormalni cijenaUbrzani cijenaBrzi popustSuN popustWebMob uvecanjeVlak' (npr. 'CVP 0,10 0,12 0,15 20,0 10,0 10,0').");
            return;
        }

        String[] parts = input.split("\\s+");
        try {
            double cijenaNormalni = Double.parseDouble(parts[1].replace(',', '.'));
            double cijenaUbrzani = Double.parseDouble(parts[2].replace(',', '.'));
            double cijenaBrzi = Double.parseDouble(parts[3].replace(',', '.'));
            double popustSuN = Double.parseDouble(parts[4].replace(',', '.'));
            double popustWebMob = Double.parseDouble(parts[5].replace(',', '.'));
            double uvecanjeVlak = Double.parseDouble(parts[6].replace(',', '.'));

            config.setPricing(cijenaNormalni, cijenaUbrzani, cijenaBrzi, popustSuN, popustWebMob, uvecanjeVlak);
        } catch (NumberFormatException e) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Vrijednosti moraju biti numeričke. " + e.getMessage());
        }
    }

    private boolean validateInput(String input) {
        return input.matches("^CVP(\\s+\\d+,\\d+){6}$");
    }
}