package org.uzdiz.memento;

import java.time.LocalDate;

public class TicketDetails {
    private String oznakaVlaka;
    private String polaznaStanica;
    private String odredisnaStanica;
    private LocalDate datum;
    private String nacinKupovine;
    private Double izvornaCijena;
    private String vrijemePolaska;
    private String vrijemeDolaska;
    private String vrijemeKupovineKarte;
    private Double popustiIznos;
    private Double konacnaCijena;

    public TicketDetails(String oznakaVlaka, String polaznaStanica, String odredisnaStanica, LocalDate datum, String nacinKupovine, Double izvornaCijena, String vrijemePolaska, String vrijemeDolaska, String vrijemeKupovineKarte, Double popustiIznos, Double konacnaCijena) {
        this.oznakaVlaka = oznakaVlaka;
        this.polaznaStanica = polaznaStanica;
        this.odredisnaStanica = odredisnaStanica;
        this.datum = datum;
        this.nacinKupovine = nacinKupovine;
        this.izvornaCijena = izvornaCijena;
        this.vrijemePolaska = vrijemePolaska;
        this.vrijemeDolaska = vrijemeDolaska;
        this.vrijemeKupovineKarte = vrijemeKupovineKarte;
        this.popustiIznos = popustiIznos;
        this.konacnaCijena = konacnaCijena;
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

    public Double getIzvornaCijena() {
        return izvornaCijena;
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

    public Double getPopustiIznos() {
        return popustiIznos;
    }

    public Double getKonacnaCijena() {
        return konacnaCijena;
    }
}
