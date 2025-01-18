package org.uzdiz.mediator;

import org.uzdiz.user.User;

public interface NotificationMediator {
    void registerSubscription(String trainId, User user);

    void sendNotification(String trainId, String message);
}
