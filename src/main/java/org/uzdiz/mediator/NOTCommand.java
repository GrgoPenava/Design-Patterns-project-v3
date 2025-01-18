package org.uzdiz.mediator;

import org.uzdiz.ConfigManager;
import org.uzdiz.userInput.CommandHandlerChain;

public class NOTCommand extends CommandHandlerChain {
    private NotificationTower notificationTower;
    ConfigManager config = ConfigManager.getInstance();

    public NOTCommand(NotificationMediator mediator) {
        this.notificationTower = new NotificationTower(mediator);
    }

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^NOT(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length != 3) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Neispravan format naredbe. Očekuje se 'NOT brojVlaka minute'.");
            return;
        }

        String trainId = parts[1];
        int delayMinutes;

        try {
            delayMinutes = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Vrijeme kašnjenja mora biti broj.");
            return;
        }

        notificationTower.sendDelayNotification(trainId, delayMinutes);
    }
}
