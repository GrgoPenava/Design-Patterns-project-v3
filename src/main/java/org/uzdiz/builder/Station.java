package org.uzdiz.builder;

public class Station {
    private Integer id;
    private String naziv;
    private String oznakaPruge;
    private String vrstaStanice;
    private String statusStanice;
    private boolean putnici;
    private boolean roba;
    private String kategorijaPruge;
    private int brojPerona;
    private String vrstaPruge;
    private int brojKolosjeka;
    private double DOPoOsovini;
    private double DOPoDuznomM;
    private String statusPruge;
    private Integer duzina;
    private Integer vrijemeNormalniVlak;
    private Integer vrijemeUbrzaniVlak;
    private Integer vrijemeBrziVlak;

    private Station(StationBuilder builder) {
        this.id = builder.id;
        this.naziv = builder.naziv;
        this.oznakaPruge = builder.oznakaPruge;
        this.vrstaStanice = builder.vrstaStanice;
        this.statusStanice = builder.statusStanice;
        this.putnici = builder.putnici;
        this.roba = builder.roba;
        this.kategorijaPruge = builder.kategorijaPruge;
        this.brojPerona = builder.brojPerona;
        this.vrstaPruge = builder.vrstaPruge;
        this.brojKolosjeka = builder.brojKolosjeka;
        this.DOPoOsovini = builder.DOPoOsovini;
        this.DOPoDuznomM = builder.DOPoDuznomM;
        this.statusPruge = builder.statusPruge;
        this.duzina = builder.duzina;
        this.vrijemeNormalniVlak = builder.vrijemeNormalniVlak;
        this.vrijemeUbrzaniVlak = builder.vrijemeUbrzaniVlak;
        this.vrijemeBrziVlak = builder.vrijemeBrziVlak;
    }

    public Integer getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getOznakaPruge() {
        return oznakaPruge;
    }

    public String getVrstaStanice() {
        return vrstaStanice;
    }

    public String getStatusStanice() {
        return statusStanice;
    }

    public boolean isPutnici() {
        return putnici;
    }

    public boolean isRoba() {
        return roba;
    }

    public String getKategorijaPruge() {
        return kategorijaPruge;
    }

    public int getBrojPerona() {
        return brojPerona;
    }

    public String getVrstaPruge() {
        return vrstaPruge;
    }

    public int getBrojKolosjeka() {
        return brojKolosjeka;
    }

    public double getDOPoOsovini() {
        return DOPoOsovini;
    }

    public double getDOPoDuznomM() {
        return DOPoDuznomM;
    }

    public String getStatusPruge() {
        return statusPruge;
    }

    public int getDuzina() {
        return duzina;
    }

    public void setDuzina(int duzina) {
        this.duzina = duzina;
    }

    public Integer getVrijemeNormalniVlak() {
        return vrijemeNormalniVlak;
    }

    public Integer getVrijemeUbrzaniVlak() {
        return vrijemeUbrzaniVlak;
    }

    public Integer getVrijemeBrziVlak() {
        return vrijemeBrziVlak;
    }

    public static class StationBuilder {
        private Integer id;
        private String naziv;
        private String oznakaPruge;
        private String vrstaStanice;
        private String statusStanice;
        private boolean putnici;
        private boolean roba;
        private String kategorijaPruge;
        private int brojPerona;
        private String vrstaPruge;
        private int brojKolosjeka;
        private double DOPoOsovini;
        private double DOPoDuznomM;
        private String statusPruge;
        private int duzina;
        private Integer vrijemeNormalniVlak;
        private Integer vrijemeUbrzaniVlak;
        private Integer vrijemeBrziVlak;

        public StationBuilder setObavezniParametri(Integer id, String naziv, String oznakaPruge, String vrstaStanice,
                                                   String statusStanice, boolean putnici, boolean roba,
                                                   String kategorijaPruge, int brojPerona, String vrstaPruge,
                                                   int brojKolosjeka, double DOPoOsovini, double DOPoDuznomM,
                                                   String statusPruge, int duzina) {
            this.id = id;
            this.naziv = naziv;
            this.oznakaPruge = oznakaPruge;
            this.vrstaStanice = vrstaStanice;
            this.statusStanice = statusStanice;
            this.putnici = putnici;
            this.roba = roba;
            this.kategorijaPruge = kategorijaPruge;
            this.brojPerona = brojPerona;
            this.vrstaPruge = vrstaPruge;
            this.brojKolosjeka = brojKolosjeka;
            this.DOPoOsovini = DOPoOsovini;
            this.DOPoDuznomM = DOPoDuznomM;
            this.statusPruge = statusPruge;
            this.duzina = duzina;
            return this;
        }

        public StationBuilder setVrijemeNormalniVlak(Integer vrijemeNormalniVlak) {
            this.vrijemeNormalniVlak = vrijemeNormalniVlak;
            return this;
        }

        public StationBuilder setVrijemeUbrzaniVlak(Integer vrijemeUbrzaniVlak) {
            this.vrijemeUbrzaniVlak = vrijemeUbrzaniVlak;
            return this;
        }

        public StationBuilder setVrijemeBrziVlak(Integer vrijemeBrziVlak) {
            this.vrijemeBrziVlak = vrijemeBrziVlak;
            return this;
        }

        public Station build() {
            return new Station(this);
        }
    }
}
