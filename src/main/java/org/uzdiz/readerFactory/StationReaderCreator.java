package org.uzdiz.readerFactory;

public class StationReaderCreator extends CsvReaderCreator {
    public StationReaderProduct factoryMethod() {
        return new StationReaderProduct();
    }
}
