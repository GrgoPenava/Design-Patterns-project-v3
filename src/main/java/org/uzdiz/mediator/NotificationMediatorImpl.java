package org.uzdiz.mediator;

import org.uzdiz.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationMediatorImpl implements NotificationMediator {
    private Map<String, List<User>> trainSubscriptions = new HashMap<>();

    @Override
    public void registerSubscription(String trainId, User user) {
        trainSubscriptions.computeIfAbsent(trainId, k -> new ArrayList<>()).add(user);
    }

    @Override
    public void sendNotification(String trainId, String message) {
        List<User> users = trainSubscriptions.getOrDefault(trainId, new ArrayList<>());

        if (users.isEmpty()) {
            System.out.println("Nema pretplaćenih korisnika za vlak " + trainId);
            return;
        }

        for (User user : users) {
            user.update(message);
        }
    }
}
