package org.uzdiz.user;

import org.uzdiz.observer.Observer;

public class User implements Observer {
    private static Integer counter = 0;
    private Integer id = 0;
    private String ime;
    private String prezime;

    public User(String ime, String prezime) {
        this.id = ++counter;
        this.ime = ime;
        this.prezime = prezime;
    }

    public String getIme() {
        return ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public void update(String message) {
        System.out.println("--> Obavijest za korisnika " + ime + " " + prezime + ": " + message);
    }
}
