package org.uzdiz.command;

import org.uzdiz.timeTableComposite.Train;
import org.uzdiz.user.User;

public class SitCommand implements Command {
    private Train train;
    private User user;
    private Receiver receiver;

    public SitCommand(Train train, User user, Receiver receiver) {
        this.train = train;
        this.user = user;
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        if (train.addPassenger(user)) {
            String message = user.getIme() + " " + user.getPrezime() + " je sjeo u vlak " + train.getOznaka();
            System.out.println(message);
            receiver.logEntry(message);
        } else {
            System.out.println("Korisnik " + user.getIme() + " " + user.getPrezime() + " već je u vlaku " + train.getOznaka() + ".");
        }
    }

    @Override
    public void undo() {
        if (train.removePassenger(user)) {
            String message = user.getIme() + " " + user.getPrezime() + " je izašao iz vlaka " + train.getOznaka();
            System.out.println(message);
            receiver.logEntry(message);
        }
    }
}

