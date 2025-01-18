package org.uzdiz.mediator;

public class NotificationTower {
    private NotificationMediator mediator;

    public NotificationTower(NotificationMediator mediator) {
        this.mediator = mediator;
    }

    public void sendDelayNotification(String trainId, int delayMinutes) {
        String message = "Vlak " + trainId + " kasni " + delayMinutes + " minuta.";
        System.out.println("Kontrolni toranj: Slanje obavijesti za vlak " + trainId);
        mediator.sendNotification(trainId, message);
    }
}
