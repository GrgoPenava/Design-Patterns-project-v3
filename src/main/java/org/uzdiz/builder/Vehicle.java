package org.uzdiz.builder;

public class Vehicle {
    private String oznaka;
    private String opis;
    private String proizvodac;
    private String godina;
    private String namjera;
    private String vrstaPrijevoza;
    private String vrstaPogona;
    private String maksBrzina;
    private String maksSnaga;
    private String brojSjedecihMjesta;
    private String brojStajacihMjesta;
    private String brojBicikala;
    private String brojKreveta;
    private String brojAutomobila;

    public String getOznaka() {
        return oznaka;
    }

    public String getOpis() {
        return opis;
    }

    public String getProizvodac() {
        return proizvodac;
    }

    public String getGodina() {
        return godina;
    }

    public String getNamjera() {
        return namjera;
    }

    public String getVrstaPrijevoza() {
        return vrstaPrijevoza;
    }

    public String getVrstaPogona() {
        return vrstaPogona;
    }

    public String getMaksBrzina() {
        return maksBrzina;
    }

    public String getMaksSnaga() {
        return maksSnaga;
    }

    public String getBrojSjedecihMjesta() {
        return brojSjedecihMjesta;
    }

    public String getBrojStajacihMjesta() {
        return brojStajacihMjesta;
    }

    public String getBrojBicikala() {
        return brojBicikala;
    }

    public String getBrojKreveta() {
        return brojKreveta;
    }

    public String getBrojAutomobila() {
        return brojAutomobila;
    }

    public String getNosivost() {
        return nosivost;
    }

    public String getPovrsina() {
        return povrsina;
    }

    public String getZapremnina() {
        return zapremnina;
    }

    public String getStatus() {
        return status;
    }

    private String nosivost;
    private String povrsina;
    private String zapremnina;
    private String status;

    private Vehicle(VehicleBuilder builder) {
        this.oznaka = builder.oznaka;
        this.opis = builder.opis;
        this.proizvodac = builder.proizvodac;
        this.godina = builder.godina;
        this.namjera = builder.namjera;
        this.vrstaPrijevoza = builder.vrstaPrijevoza;
        this.vrstaPogona = builder.vrstaPogona;
        this.maksBrzina = builder.maksBrzina;
        this.maksSnaga = builder.maksSnaga;
        this.brojSjedecihMjesta = builder.brojSjedecihMjesta;
        this.brojStajacihMjesta = builder.brojStajacihMjesta;
        this.brojBicikala = builder.brojBicikala;
        this.brojKreveta = builder.brojKreveta;
        this.brojAutomobila = builder.brojAutomobila;
        this.nosivost = builder.nosivost;
        this.povrsina = builder.povrsina;
        this.zapremnina = builder.zapremnina;
        this.status = builder.status;
    }

    public static class VehicleBuilder {
        private String oznaka;
        private String opis;

        private String proizvodac;
        private String godina;
        private String namjera;
        private String vrstaPrijevoza;
        private String vrstaPogona;
        private String maksBrzina;
        private String maksSnaga;
        private String brojSjedecihMjesta;
        private String brojStajacihMjesta;
        private String brojBicikala;
        private String brojKreveta;
        private String brojAutomobila;
        private String nosivost;
        private String povrsina;
        private String zapremnina;
        private String status;

        public VehicleBuilder(String oznaka, String opis) {
            this.oznaka = oznaka;
            this.opis = opis;
        }

        public VehicleBuilder setProizvodac(String proizvodac) {
            this.proizvodac = proizvodac;
            return this;
        }

        public VehicleBuilder setGodina(String godina) {
            this.godina = godina;
            return this;
        }

        public VehicleBuilder setNamjera(String namjera) {
            this.namjera = namjera;
            return this;
        }

        public VehicleBuilder setVrstaPrijevoza(String vrstaPrijevoza) {
            this.vrstaPrijevoza = vrstaPrijevoza;
            return this;
        }

        public VehicleBuilder setVrstaPogona(String vrstaPogona) {
            this.vrstaPogona = vrstaPogona;
            return this;
        }

        public VehicleBuilder setMaksBrzina(String maksBrzina) {
            this.maksBrzina = maksBrzina;
            return this;
        }

        public VehicleBuilder setMaksSnaga(String maksSnaga) {
            this.maksSnaga = maksSnaga;
            return this;
        }

        public VehicleBuilder setBrojSjedecihMjesta(String brojSjedecihMjesta) {
            this.brojSjedecihMjesta = brojSjedecihMjesta;
            return this;
        }

        public VehicleBuilder setBrojStajacihMjesta(String brojStajacihMjesta) {
            this.brojStajacihMjesta = brojStajacihMjesta;
            return this;
        }

        public VehicleBuilder setBrojBicikala(String brojBicikala) {
            this.brojBicikala = brojBicikala;
            return this;
        }

        public VehicleBuilder setBrojKreveta(String brojKreveta) {
            this.brojKreveta = brojKreveta;
            return this;
        }

        public VehicleBuilder setBrojAutomobila(String brojAutomobila) {
            this.brojAutomobila = brojAutomobila;
            return this;
        }

        public VehicleBuilder setNosivost(String nosivost) {
            this.nosivost = nosivost;
            return this;
        }

        public VehicleBuilder setPovrsina(String povrsina) {
            this.povrsina = povrsina;
            return this;
        }

        public VehicleBuilder setZapremnina(String zapremnina) {
            this.zapremnina = zapremnina;
            return this;
        }

        public VehicleBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(this);
        }
    }
}

