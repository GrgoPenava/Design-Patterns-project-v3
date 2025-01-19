package org.uzdiz.memento;

import java.time.LocalDate;

public class TicketDetails {
    private String oznakaVlaka;
    private String polaznaStanica;
    private String odredisnaStanica;
    private LocalDate datum;
    private String nacinKupovine;
    private Double cijena;
    private String vrijemePolaska;
    private String vrijemeDolaska;
    private String vrijemeKupovineKarte;

    public TicketDetails(String oznakaVlaka, String polaznaStanica, String odredisnaStanica, LocalDate datum, String nacinKupovine, Double cijena, String vrijemePolaska, String vrijemeDolaska, String vrijemeKupovineKarte) {
            this.oznakaVlaka = oznakaVlaka;
            this.polaznaStanica = polaznaStanica;
            this.odredisnaStanica = odredisnaStanica;
            this.datum = datum;
            this.nacinKupovine = nacinKupovine;
            this.cijena = cijena;
            this.vrijemePolaska = vrijemePolaska;
            this.vrijemeDolaska = vrijemeDolaska;
            this.vrijemeKupovineKarte = vrijemeKupovineKarte;
    }

    public String getTicketOznakaVlaka() {
        return oznakaVlaka;
    }

    public String getPolaznaStanica() {
        return polaznaStanica;
    }
    public String getOdredisnaStanica() {
        return odredisnaStanica;
    }
    public LocalDate getDatum() {
        return datum;
    }
    public String getNacinKupovine() {
        return nacinKupovine;
    }
    public Double getCijena() {
        return cijena;
    }
    public String getVrijemePolaska() {
        return vrijemePolaska;
    }
    public String getVrijemeDolaska() {
        return vrijemeDolaska;
    }

    public String getVrijemeKupovineKarte() {
        return vrijemeKupovineKarte;
    }
}
