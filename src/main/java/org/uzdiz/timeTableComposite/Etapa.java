package org.uzdiz.timeTableComposite;

import org.uzdiz.builder.Station;

import java.util.ArrayList;
import java.util.List;

public class Etapa extends TimeTableComposite {
    private String oznakaPruge;
    private String pocetnaStanica;
    private String odredisnaStanica;
    private String vrijemePolaska;
    private String trajanjeVoznje;
    private String oznakaDana;
    private String smjer;
    private List<Station> listaStanicaKojeNeVoze = new ArrayList<>();

    public Etapa(String oznaka, String oznakaPruge, String pocetnaStanica, String odredisnaStanica, String vrijemePolaska, String trajanjeVoznje, String oznakaDana, String smjer) {
        super(oznaka);
        this.oznakaPruge = oznakaPruge;
        this.pocetnaStanica = pocetnaStanica;
        this.odredisnaStanica = odredisnaStanica;
        this.vrijemePolaska = vrijemePolaska;
        this.trajanjeVoznje = trajanjeVoznje;
        this.oznakaDana = oznakaDana;
        this.smjer = smjer;
    }

    public String getSmjer() {
        return smjer;
    }

    public String getOznakaPruge() {
        return oznakaPruge;
    }

    public void setOznakaPruge(String oznakaPruge) {
        this.oznakaPruge = oznakaPruge;
    }

    public String getPocetnaStanica() {
        return pocetnaStanica;
    }

    public void setPocetnaStanica(String pocetnaStanica) {
        this.pocetnaStanica = pocetnaStanica;
    }

    public String getOdredisnaStanica() {
        return odredisnaStanica;
    }

    public void setOdredisnaStanica(String odredisnaStanica) {
        this.odredisnaStanica = odredisnaStanica;
    }

    public String getVrijemePolaska() {
        return vrijemePolaska;
    }

    public void setVrijemePolaska(String vrijemePolaska) {
        this.vrijemePolaska = vrijemePolaska;
    }

    public String getTrajanjeVoznje() {
        return trajanjeVoznje;
    }

    public void setTrajanjeVoznje(String trajanjeVoznje) {
        this.trajanjeVoznje = trajanjeVoznje;
    }

    public String getOznakaDana() {
        return oznakaDana;
    }

    public void setOznakaDana(String oznakaDana) {
        this.oznakaDana = oznakaDana;
    }

    public List<Station> getListaStanicaKojeNeVoze() {
        return listaStanicaKojeNeVoze;
    }

    public void setListaStanicaKojeNeVoze(Station novaStanica) {
        boolean exists = false;
        for (Station s : listaStanicaKojeNeVoze) {
            if (s.getNaziv().equals(novaStanica.getNaziv())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            System.out.println(novaStanica.getNaziv());
            this.listaStanicaKojeNeVoze.add(novaStanica);
        }
    }

    public void ocistiListuStanica() {
        this.listaStanicaKojeNeVoze.clear();
    }
}
