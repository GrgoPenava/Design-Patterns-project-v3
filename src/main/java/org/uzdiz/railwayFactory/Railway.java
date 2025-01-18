package org.uzdiz.railwayFactory;

import org.uzdiz.builder.Station;

import java.util.ArrayList;
import java.util.List;

public abstract class Railway {
    protected String oznakaPruge;
    protected String kategorija;
    protected String nacinPrijevoza;
    protected int brojKolosjeka;
    protected double duljina;
    protected double dozvoljenoOpterecenjePoOsovini;
    protected double dozvoljenoOpterecenjePoDuznomMetru;
    protected String status;
    protected List<Station> popisSvihStanica;

    public Railway(String oznakaPruge) {
        this.oznakaPruge = oznakaPruge;
        this.popisSvihStanica = new ArrayList<>();
    }

    public List<Station> getPopisSvihStanica() {
        return popisSvihStanica;
    }

    public void setPopisSvihStanica(ArrayList<Station> popisSvihStanica) {
        this.popisSvihStanica = popisSvihStanica;
    }

    public void addStation(Station station) {
        this.popisSvihStanica.add(station);
    }

    public String getOznakaPruge() {
        return oznakaPruge;
    }

    public void setOznakaPruge(String oznakaPruge) {
        this.oznakaPruge = oznakaPruge;
    }

    public String getKategorija() {
        return kategorija;
    }

    public void setKategorija(String kategorija) {
        this.kategorija = kategorija;
    }

    public String getNacinPrijevoza() {
        return nacinPrijevoza;
    }

    public void setNacinPrijevoza(String nacinPrijevoza) {
        this.nacinPrijevoza = nacinPrijevoza;
    }

    public int getBrojKolosjeka() {
        return brojKolosjeka;
    }

    public void setBrojKolosjeka(int brojKolosjeka) {
        this.brojKolosjeka = brojKolosjeka;
    }

    public double getDuljina() {
        return duljina;
    }

    public void setDuljina(double duljina) {
        this.duljina = duljina;
    }

    public double getDozvoljenoOpterecenjePoOsovini() {
        return dozvoljenoOpterecenjePoOsovini;
    }

    public void setDozvoljenoOpterecenjePoOsovini(double dozvoljenoOpterecenjePoOsovini) {
        this.dozvoljenoOpterecenjePoOsovini = dozvoljenoOpterecenjePoOsovini;
    }

    public double getDozvoljenoOpterecenjePoDuznomMetru() {
        return dozvoljenoOpterecenjePoDuznomMetru;
    }

    public void setDozvoljenoOpterecenjePoDuznomMetru(double dozvoljenoOpterecenjePoDuznomMetru) {
        this.dozvoljenoOpterecenjePoDuznomMetru = dozvoljenoOpterecenjePoDuznomMetru;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
}
