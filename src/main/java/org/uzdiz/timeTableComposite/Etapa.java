package org.uzdiz.timeTableComposite;

public class Etapa extends TimeTableComposite {
    private String oznakaPruge;
    private String pocetnaStanica;
    private String odredisnaStanica;
    private String vrijemePolaska;
    private String trajanjeVoznje;
    private String oznakaDana;
    private String smjer;

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
}
