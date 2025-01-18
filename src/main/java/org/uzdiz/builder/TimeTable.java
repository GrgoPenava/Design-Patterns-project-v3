package org.uzdiz.builder;

public class TimeTable {
    private String oznakaPruge;
    private String smjer;
    private String polaznaStanica;
    private String odredisnaStanica;
    private String oznakaVlaka;
    private String vrstaVlaka;
    private String vrijemePolaska;
    private String trajanjeVoznje;
    private String oznakaDana;

    private TimeTable(TimeTableBuilder builder) {
        this.oznakaPruge = builder.oznakaPruge;
        this.smjer = builder.smjer;
        this.polaznaStanica = builder.polaznaStanica;
        this.odredisnaStanica = builder.odredisnaStanica;
        this.oznakaVlaka = builder.oznakaVlaka;
        this.vrstaVlaka = builder.vrstaVlaka;
        this.vrijemePolaska = builder.vrijemePolaska;
        this.trajanjeVoznje = builder.trajanjeVoznje;
        this.oznakaDana = builder.oznakaDana;
    }

    public String getOznakaPruge() {
        return oznakaPruge;
    }

    public String getSmjer() {
        return smjer;
    }

    public String getPolaznaStanica() {
        return polaznaStanica;
    }

    public String getOdredisnaStanica() {
        return odredisnaStanica;
    }

    public String getOznakaVlaka() {
        return oznakaVlaka;
    }

    public String getVrstaVlaka() {
        return vrstaVlaka;
    }

    public String getVrijemePolaska() {
        return vrijemePolaska;
    }

    public String getTrajanjeVoznje() {
        return trajanjeVoznje;
    }

    public String getOznakaDana() {
        return oznakaDana;
    }

    public static class TimeTableBuilder {
        private String oznakaPruge;
        private String smjer;
        private String polaznaStanica;
        private String odredisnaStanica;
        private String oznakaVlaka;
        private String vrstaVlaka;
        private String vrijemePolaska;
        private String trajanjeVoznje;
        private String oznakaDana;

        public TimeTableBuilder(String oznakaPruge, String smjer, String oznakaVlaka, String vrijemePolaska) {
            this.oznakaPruge = oznakaPruge;
            this.smjer = smjer;
            this.oznakaVlaka = oznakaVlaka;
            this.vrijemePolaska = vrijemePolaska;
        }

        public TimeTableBuilder setPolaznaStanica(String polaznaStanica) {
            this.polaznaStanica = polaznaStanica;
            return this;
        }

        public TimeTableBuilder setOdredisnaStanica(String odredisnaStanica) {
            this.odredisnaStanica = odredisnaStanica;
            return this;
        }

        public TimeTableBuilder setVrstaVlaka(String vrstaVlaka) {
            this.vrstaVlaka = vrstaVlaka;
            return this;
        }

        public TimeTableBuilder setTrajanjeVoznje(String trajanjeVoznje) {
            this.trajanjeVoznje = trajanjeVoznje;
            return this;
        }

        public TimeTableBuilder setOznakaDana(String oznakaDana) {
            this.oznakaDana = oznakaDana;
            return this;
        }

        public TimeTable build() {
            return new TimeTable(this);
        }
    }
}
