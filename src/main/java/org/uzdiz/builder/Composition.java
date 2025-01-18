package org.uzdiz.builder;

public class Composition {
    private String oznaka;
    private String oznakaVozila;
    private String uloga;

    private Composition(CompositionBuilder builder) {
        this.oznaka = builder.oznaka;
        this.oznakaVozila = builder.oznakaVozila;
        this.uloga = builder.uloga;
    }

    public String getOznaka() {
        return oznaka;
    }

    public String getOznakaVozila() {
        return oznakaVozila;
    }

    public String getUloga() {
        return uloga;
    }

    public static class CompositionBuilder {
        private String oznaka;
        private String oznakaVozila;
        private String uloga;

        public CompositionBuilder(String oznaka) {
            this.oznaka = oznaka;
        }

        public CompositionBuilder setOznakaVozila(String oznaka) {
            this.oznakaVozila = oznaka;
            return this;
        }

        public CompositionBuilder setUloga(String uloga) {
            this.uloga = uloga;
            return this;
        }

        public Composition build() {
            return new Composition(this);
        }
    }
}

